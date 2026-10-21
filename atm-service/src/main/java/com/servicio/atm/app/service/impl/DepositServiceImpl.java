package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.DepositService;
import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DepositServiceImpl implements DepositService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public DepositServiceImpl(CardRepository cardRepository,
                              AccountRepository accountRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public BigDecimal deposit(String cardNumber, String cbu, BigDecimal amount) {
        Card card = cardRepository.findByNumero(cardNumber)
                .orElseThrow(TarjetaInactivaException::new);

        if (!Boolean.TRUE.equals(card.getActiva())) {
            throw new TarjetaInactivaException();
        }

        // La cuenta destino se busca por CBU y puede ser de otra persona.
        Account cuenta = accountRepository.findByCbuForUpdate(cbu)
                .orElseThrow(CuentaNoEncontradaException::new);

        cuenta.acreditar(amount);

        return cuenta.getSaldo();
    }
}