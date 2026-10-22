package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.LoginService;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.infra.out.logging.TransactionLogger;
import com.servicio.atm.infra.out.persistence.CardRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Identificacion con tarjeta: verifica que exista y este activa.
 * Cada intento, exitoso o no, queda en el log transaccional.
 */
@Service
public class LoginServiceImpl implements LoginService {

    private final CardRepository cardRepository;
    private final TransactionLogger transactionLogger;

    public LoginServiceImpl(CardRepository cardRepository, TransactionLogger transactionLogger) {
        this.cardRepository = cardRepository;
        this.transactionLogger = transactionLogger;
    }

    @Override
    public Boolean login(String cardNumber) {
        Optional<Card> card = cardRepository.findByNumero(cardNumber);
        boolean exitoso = card.isPresent() && Boolean.TRUE.equals(card.get().getActiva());
        transactionLogger.registrarLogin(cardNumber, exitoso);
        return exitoso;
    }
}
