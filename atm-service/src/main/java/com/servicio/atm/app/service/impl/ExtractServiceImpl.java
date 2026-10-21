package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.ExtractService;
import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.CardAccount;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.SaldoInsuficienteException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ExtractServiceImpl implements ExtractService {

    private final CardAccountRepository cardAccountRepository;
    private final AccountRepository accountRepository;

    public ExtractServiceImpl(CardAccountRepository cardAccountRepository,
                              AccountRepository accountRepository) {
        this.cardAccountRepository = cardAccountRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public BigDecimal extract(String cardNumber, String accountNumber, BigDecimal amount) {
        CardAccount cardAccount = cardAccountRepository
                .findByTarjeta_NumeroAndCuenta_Numero(cardNumber, accountNumber)
                .orElseThrow(CuentaNoEncontradaException::new);

        if (!Boolean.TRUE.equals(cardAccount.getTarjeta().getActiva())) {
            throw new TarjetaInactivaException();
        }

        // Bloqueo pesimista: otra extraccion sobre la misma cuenta espera aqui
        // hasta que esta transaccion haga commit.
        Account cuenta = accountRepository.findByIdForUpdate(cardAccount.getCuenta().getId())
                .orElseThrow(CuentaNoEncontradaException::new);

        if (!cuenta.debitar(amount)) {
            throw new SaldoInsuficienteException();
        }

        return cuenta.getSaldo();
    }
}