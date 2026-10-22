package com.servicio.atm.app.service.impl;

import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.domain.entity.CardAccount;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.infra.out.persistence.CardAccountRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    private static final String TARJETA = "4000123412341234";
    private static final String CUENTA = "0001";

    @Mock
    private CardAccountRepository cardAccountRepository;

    @InjectMocks
    private BalanceServiceImpl service;

    @Test
    @DisplayName("devuelve el saldo de la cuenta asociada a la tarjeta")
    void devuelveElSaldo() {
        Account cuenta = new Account(10L, CUENTA, "0000000000000000000001", new BigDecimal("1234.56"));
        Card card = Card.builder().id(1L).numero(TARJETA).activa(true).build();
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.of(new CardAccount(1L, card, cuenta)));

        BigDecimal saldo = service.getBalance(TARJETA, CUENTA);

        assertEquals(0, new BigDecimal("1234.56").compareTo(saldo));
    }

    @Test
    @DisplayName("lanza CuentaNoEncontradaException si la cuenta no corresponde a la tarjeta")
    void cuentaNoEncontrada() {
        when(cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(TARJETA, CUENTA))
                .thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> service.getBalance(TARJETA, CUENTA));
    }
}
