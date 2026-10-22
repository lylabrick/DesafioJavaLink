package com.servicio.atm.app.service.impl;

import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.domain.entity.CardAccount;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.SaldoInsuficienteException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.infra.out.logging.TransactionLogger;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractServiceImplTest {

    private static final String TARJETA = "4000123412341234";
    private static final String CUENTA = "0001";
    private static final String TIPO = "EXTRACCION";

    @Mock
    private CardAccountRepository cardAccountRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogger transactionLogger;

    @InjectMocks
    private ExtractServiceImpl service;

    private Account cuenta;

    @BeforeEach
    void setUp() {
        cuenta = new Account(10L, CUENTA, "0000000000000000000001", new BigDecimal("500.00"));
    }

    private CardAccount asociacion(boolean tarjetaActiva) {
        Card card = Card.builder().id(1L).numero(TARJETA).activa(tarjetaActiva).build();
        return new CardAccount(1L, card, cuenta);
    }

    @Test
    @DisplayName("extrae el importe, devuelve el saldo resultante y registra la operacion como OK")
    void extraeElImporte() {
        BigDecimal importe = new BigDecimal("100.00");
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.of(asociacion(true)));
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cuenta));

        BigDecimal saldo = service.extract(TARJETA, CUENTA, importe);

        assertEquals(0, new BigDecimal("400.00").compareTo(saldo));
        assertEquals(0, new BigDecimal("400.00").compareTo(cuenta.getSaldo()));
        verify(transactionLogger).registrar(TIPO, TARJETA, CUENTA, importe, "OK");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("lanza CuentaNoEncontradaException y registra el rechazo si la cuenta no corresponde a la tarjeta")
    void cuentaNoAsociadaALaTarjeta() {
        BigDecimal importe = new BigDecimal("100.00");
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> service.extract(TARJETA, CUENTA, importe));

        verifyNoInteractions(accountRepository);
        verify(transactionLogger).registrar(TIPO, TARJETA, CUENTA, importe, "RECHAZADA(CuentaNoEncontradaException)");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("lanza TarjetaInactivaException, no bloquea la cuenta y registra el rechazo")
    void tarjetaInactiva() {
        BigDecimal importe = new BigDecimal("100.00");
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.of(asociacion(false)));

        assertThrows(TarjetaInactivaException.class,
                () -> service.extract(TARJETA, CUENTA, importe));

        verifyNoInteractions(accountRepository);
        assertEquals(0, new BigDecimal("500.00").compareTo(cuenta.getSaldo()));
        verify(transactionLogger).registrar(TIPO, TARJETA, CUENTA, importe, "RECHAZADA(TarjetaInactivaException)");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("lanza CuentaNoEncontradaException y registra el rechazo si la cuenta desaparece al bloquearla")
    void cuentaNoEncontradaAlBloquear() {
        BigDecimal importe = new BigDecimal("100.00");
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.of(asociacion(true)));
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> service.extract(TARJETA, CUENTA, importe));

        verify(transactionLogger).registrar(TIPO, TARJETA, CUENTA, importe, "RECHAZADA(CuentaNoEncontradaException)");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("lanza SaldoInsuficienteException, no modifica el saldo y registra el rechazo")
    void saldoInsuficiente() {
        BigDecimal importe = new BigDecimal("500.01");
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.of(asociacion(true)));
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cuenta));

        assertThrows(SaldoInsuficienteException.class,
                () -> service.extract(TARJETA, CUENTA, importe));

        assertEquals(0, new BigDecimal("500.00").compareTo(cuenta.getSaldo()));
        verify(transactionLogger).registrar(TIPO, TARJETA, CUENTA, importe, "RECHAZADA(SaldoInsuficienteException)");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("registra el rechazo y propaga IllegalArgumentException si el importe no es valido")
    void importeInvalido() {
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.of(asociacion(true)));
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cuenta));

        assertThrows(IllegalArgumentException.class,
                () -> service.extract(TARJETA, CUENTA, BigDecimal.ZERO));

        verify(transactionLogger).registrar(TIPO, TARJETA, CUENTA, BigDecimal.ZERO, "RECHAZADA(IllegalArgumentException)");
        verifyNoMoreInteractions(transactionLogger);
    }
}
