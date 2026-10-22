package com.servicio.atm.app.service;

import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.domain.entity.CardAccount;
import com.servicio.atm.domain.exception.SaldoInsuficienteException;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardAccountRepository;
import com.servicio.atm.infra.out.persistence.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integracion contra H2: verifica que el bloqueo pesimista serializa las
 * operaciones concurrentes sobre una misma cuenta y que el saldo final es correcto.
 * Los datos se crean dentro de cada test.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:atmconcurrency;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=20000")
class TransactionConcurrencyTest {

    private static final int HILOS = 8;

    @Autowired
    private ExtractService extractService;

    @Autowired
    private DepositService depositService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardAccountRepository cardAccountRepository;

    private Card crearTarjeta(String numero) {
        return cardRepository.save(Card.builder().numero(numero).activa(true).build());
    }

    private Account crearCuenta(String numero, String cbu, String saldo) {
        return accountRepository.save(new Account(null, numero, cbu, new BigDecimal(saldo)));
    }

    /** Lanza todas las tareas a la vez y devuelve cuando todas terminaron. */
    private <T> List<Future<T>> ejecutarEnParalelo(List<Callable<T>> tareas) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tareas.size());
        CountDownLatch listos = new CountDownLatch(tareas.size());
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<T>> futuros = new ArrayList<>();

        for (Callable<T> tarea : tareas) {
            futuros.add(pool.submit(() -> {
                listos.countDown();
                largada.await();
                return tarea.call();
            }));
        }

        assertTrue(listos.await(10, TimeUnit.SECONDS), "los hilos no arrancaron a tiempo");
        largada.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "las operaciones no terminaron a tiempo");
        return futuros;
    }

    @Test
    @DisplayName("extracciones concurrentes: nunca se retira mas que el saldo y el saldo no queda negativo")
    void extraccionesConcurrentesNoDejanSaldoNegativo() throws Exception {
        Card tarjeta = crearTarjeta("4000000000000001");
        Account cuenta = crearCuenta("CONC-EXT-1", "0000000000000000001001", "500.00");
        cardAccountRepository.save(new CardAccount(null, tarjeta, cuenta));

        AtomicInteger exitosas = new AtomicInteger();
        AtomicInteger rechazadas = new AtomicInteger();

        List<Callable<Void>> tareas = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            tareas.add(() -> {
                try {
                    extractService.extract("4000000000000001", "CONC-EXT-1", new BigDecimal("100.00"));
                    exitosas.incrementAndGet();
                } catch (SaldoInsuficienteException e) {
                    rechazadas.incrementAndGet();
                }
                return null;
            });
        }

        for (Future<Void> futuro : ejecutarEnParalelo(tareas)) {
            futuro.get(); // propaga cualquier error inesperado (por ejemplo, de bloqueo)
        }

        BigDecimal saldoFinal = accountRepository.findById(cuenta.getId()).get().getSaldo();

        assertEquals(5, exitosas.get(), "con 500 de saldo solo alcanzan 5 extracciones de 100");
        assertEquals(HILOS - 5, rechazadas.get());
        assertEquals(0, BigDecimal.ZERO.compareTo(saldoFinal), "el saldo final debe ser exactamente cero");
    }

    @Test
    @DisplayName("depositos concurrentes: no se pierde ninguna actualizacion")
    void depositosConcurrentesNoPierdenActualizaciones() throws Exception {
        crearTarjeta("4000000000000002");
        Account cuenta = crearCuenta("CONC-DEP-1", "0000000000000000002001", "100.00");

        List<Callable<BigDecimal>> tareas = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            tareas.add(() -> depositService.deposit(
                    "4000000000000002", "0000000000000000002001", new BigDecimal("25.00")));
        }

        for (Future<BigDecimal> futuro : ejecutarEnParalelo(tareas)) {
            futuro.get();
        }

        BigDecimal saldoFinal = accountRepository.findById(cuenta.getId()).get().getSaldo();

        // 100 inicial + 8 depositos de 25 = 300
        assertEquals(0, new BigDecimal("300.00").compareTo(saldoFinal));
    }

    @Test
    @DisplayName("depositos y extracciones concurrentes sobre la misma cuenta: el saldo final es consistente")
    void depositosYExtraccionesConcurrentes() throws Exception {
        Card tarjeta = crearTarjeta("4000000000000003");
        Account cuenta = crearCuenta("CONC-MIX-1", "0000000000000000003001", "1000.00");
        cardAccountRepository.save(new CardAccount(null, tarjeta, cuenta));

        // Con 1000 de saldo inicial todas las extracciones de 50 alcanzan, sin importar el orden.
        List<Callable<Void>> tareas = new ArrayList<>();
        for (int i = 0; i < HILOS / 2; i++) {
            tareas.add(() -> {
                depositService.deposit("4000000000000003", "0000000000000000003001", new BigDecimal("100.00"));
                return null;
            });
            tareas.add(() -> {
                extractService.extract("4000000000000003", "CONC-MIX-1", new BigDecimal("50.00"));
                return null;
            });
        }

        for (Future<Void> futuro : ejecutarEnParalelo(tareas)) {
            futuro.get();
        }

        BigDecimal saldoFinal = accountRepository.findById(cuenta.getId()).get().getSaldo();

        // 1000 + 4 depositos de 100 - 4 extracciones de 50 = 1200
        assertEquals(0, new BigDecimal("1200.00").compareTo(saldoFinal));
    }
}
