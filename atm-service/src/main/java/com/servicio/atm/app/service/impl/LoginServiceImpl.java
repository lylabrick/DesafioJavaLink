package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.LoginService;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.infra.out.persistence.CardRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginServiceImpl implements LoginService {

    private final CardRepository cardRepository;

    public LoginServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public Boolean login(String cardNumber) {
        Optional<Card> card = cardRepository.findByNumero(cardNumber);
        return card.isPresent() && Boolean.TRUE.equals(card.get().getActiva());
    }
}