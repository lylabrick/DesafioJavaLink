package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.DepositService;
import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.CardAccount;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DepositServiceImpl implements DepositService {

    private final CardAccountRepository cardAccountRepository;
    private final AccountRepository accountRepository;

    public DepositServiceImpl(CardAccountRepository cardAccountRepository,
                              AccountRepository accountRepository) {
        this.cardAccountRepository = cardAccountRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public BigDecimal deposit(String cardNumber, String accountNumber, BigDecimal amount) {
        CardAccount cardAccount = cardAccountRepository
                .findByTarjeta_NumeroAndCuenta_Numero(cardNumber, accountNumber)
                .orElseThrow(CuentaNoEncontradaException::new);

        if (!Boolean.TRUE.equals(cardAccount.getTarjeta().getActiva())) {
            throw new TarjetaInactivaException();
        }

        Account cuenta = accountRepository.findByIdForUpdate(cardAccount.getCuenta().getId())
                .orElseThrow(CuentaNoEncontradaException::new);

        cuenta.acreditar(amount);

        return cuenta.getSaldo();
    }
}