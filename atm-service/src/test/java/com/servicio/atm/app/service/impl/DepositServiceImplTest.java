package com.servicio.atm.app.service.impl;

import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.domain.exception.TarjetaNoEncontradaException;
import com.servicio.atm.infra.out.logging.TransactionLogger;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardRepository;
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
class DepositServiceImplTest {

    private static final String TARJETA = "4000123412341234";
    private static final String CBU = "0000000000000000000099";
    private static final String TIPO = "DEPOSITO";

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogger transactionLogger;

    @InjectMocks
    private DepositServiceImpl service;

    private Account cuentaDestino;

    @BeforeEach
    void setUp() {
        cuentaDestino = new Account(20L, "0099", CBU, new BigDecimal("100.00"));
    }

    private Card tarjeta(boolean activa) {
        return Card.builder().id(1L).numero(TARJETA).activa(activa).build();
    }

    @Test
    @DisplayName("acredita el importe, devuelve el saldo resultante y registra la operacion como OK")
    void acreditaElImporte() {
        BigDecimal importe = new BigDecimal("250.50");
        when(cardRepository.findByNumero(TARJETA)).thenReturn(Optional.of(tarjeta(true)));
        when(accountRepository.findByCbuForUpdate(CBU)).thenReturn(Optional.of(cuentaDestino));

        BigDecimal saldo = service.deposit(TARJETA, CBU, importe);

        assertEquals(0, new BigDecimal("350.50").compareTo(saldo));
        assertEquals(0, new BigDecimal("350.50").compareTo(cuentaDestino.getSaldo()));
        verify(transactionLogger).registrar(TIPO, TARJETA, CBU, importe, "OK");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("la cuenta destino se obtiene solo por CBU, sin depender de la asociacion con la tarjeta")
    void cuentaDestinoSeBuscaSoloPorCbu() {
        BigDecimal importe = new BigDecimal("1.00");
        when(cardRepository.findByNumero(TARJETA)).thenReturn(Optional.of(tarjeta(true)));
        when(accountRepository.findByCbuForUpdate(CBU)).thenReturn(Optional.of(cuentaDestino));

        BigDecimal saldo = service.deposit(TARJETA, CBU, importe);

        assertEquals(0, new BigDecimal("101.00").compareTo(saldo));
        verify(accountRepository).findByCbuForUpdate(CBU);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("lanza TarjetaInactivaException, no consulta la cuenta y registra el rechazo si la tarjeta esta inactiva")
    void tarjetaInactiva() {
        BigDecimal importe = new BigDecimal("10.00");
        when(cardRepository.findByNumero(TARJETA)).thenReturn(Optional.of(tarjeta(false)));

        assertThrows(TarjetaInactivaException.class,
                () -> service.deposit(TARJETA, CBU, importe));

        verifyNoInteractions(accountRepository);
        assertEquals(0, new BigDecimal("100.00").compareTo(cuentaDestino.getSaldo()));
        verify(transactionLogger).registrar(TIPO, TARJETA, CBU, importe, "RECHAZADA(TarjetaInactivaException)");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("lanza TarjetaNoEncontradaException, no consulta la cuenta y registra el rechazo si la tarjeta no existe")
    void tarjetaInexistente() {
        BigDecimal importe = new BigDecimal("10.00");
        when(cardRepository.findByNumero(TARJETA)).thenReturn(Optional.empty());

        assertThrows(TarjetaNoEncontradaException.class,
                () -> service.deposit(TARJETA, CBU, importe));

        verifyNoInteractions(accountRepository);
        assertEquals(0, new BigDecimal("100.00").compareTo(cuentaDestino.getSaldo()));
        verify(transactionLogger).registrar(TIPO, TARJETA, CBU, importe, "RECHAZADA(TarjetaNoEncontradaException)");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("lanza CuentaNoEncontradaException y registra el rechazo si no existe una cuenta con ese CBU")
    void cbuInexistente() {
        BigDecimal importe = new BigDecimal("10.00");
        when(cardRepository.findByNumero(TARJETA)).thenReturn(Optional.of(tarjeta(true)));
        when(accountRepository.findByCbuForUpdate(CBU)).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> service.deposit(TARJETA, CBU, importe));

        verify(transactionLogger).registrar(TIPO, TARJETA, CBU, importe, "RECHAZADA(CuentaNoEncontradaException)");
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("registra el rechazo y propaga IllegalArgumentException si el importe no es valido, sin cambiar el saldo")
    void importeInvalido() {
        when(cardRepository.findByNumero(TARJETA)).thenReturn(Optional.of(tarjeta(true)));
        when(accountRepository.findByCbuForUpdate(CBU)).thenReturn(Optional.of(cuentaDestino));

        assertThrows(IllegalArgumentException.class,
                () -> service.deposit(TARJETA, CBU, BigDecimal.ZERO));

        assertEquals(0, new BigDecimal("100.00").compareTo(cuentaDestino.getSaldo()));
        verify(transactionLogger).registrar(TIPO, TARJETA, CBU, BigDecimal.ZERO, "RECHAZADA(IllegalArgumentException)");
        verifyNoMoreInteractions(transactionLogger);
    }
}
