package com.servicio.atm.app.service.impl;

import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.infra.out.logging.TransactionLogger;
import com.servicio.atm.infra.out.persistence.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    private static final String TARJETA = "4000123412341234";

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionLogger transactionLogger;

    @InjectMocks
    private LoginServiceImpl service;

    @Test
    @DisplayName("devuelve true y registra el ingreso exitoso si la tarjeta existe y esta activa")
    void tarjetaActiva() {
        when(cardRepository.findByNumero(TARJETA))
                .thenReturn(Optional.of(Card.builder().id(1L).numero(TARJETA).activa(true).build()));

        assertTrue(service.login(TARJETA));

        verify(transactionLogger).registrarLogin(TARJETA, true);
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("devuelve false y registra el ingreso no exitoso si la tarjeta esta inactiva")
    void tarjetaInactiva() {
        when(cardRepository.findByNumero(TARJETA))
                .thenReturn(Optional.of(Card.builder().id(1L).numero(TARJETA).activa(false).build()));

        assertFalse(service.login(TARJETA));

        verify(transactionLogger).registrarLogin(TARJETA, false);
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("devuelve false y registra el ingreso no exitoso si la tarjeta no tiene el indicador de actividad")
    void tarjetaSinIndicador() {
        when(cardRepository.findByNumero(TARJETA))
                .thenReturn(Optional.of(Card.builder().id(1L).numero(TARJETA).activa(null).build()));

        assertFalse(service.login(TARJETA));

        verify(transactionLogger).registrarLogin(TARJETA, false);
        verifyNoMoreInteractions(transactionLogger);
    }

    @Test
    @DisplayName("devuelve false y registra el ingreso no exitoso si la tarjeta no existe")
    void tarjetaInexistente() {
        when(cardRepository.findByNumero(TARJETA)).thenReturn(Optional.empty());

        assertFalse(service.login(TARJETA));

        verify(transactionLogger).registrarLogin(TARJETA, false);
        verifyNoMoreInteractions(transactionLogger);
    }
}
