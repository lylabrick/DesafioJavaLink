package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.BalanceService;
import com.servicio.atm.domain.entity.CardAccount;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.infra.out.persistence.CardAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Consulta de saldo en modo solo lectura: no bloquea la cuenta.
 */
@Service
public class BalanceServiceImpl implements BalanceService {

    private final CardAccountRepository cardAccountRepository;

    public BalanceServiceImpl(CardAccountRepository cardAccountRepository) {
        this.cardAccountRepository = cardAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String cardNumber, String accountNumber) {
        CardAccount cardAccount = cardAccountRepository.findByTarjeta_NumeroAndCuenta_Numero(cardNumber, accountNumber)
                .orElseThrow(CuentaNoEncontradaException::new);
        return cardAccount.getCuenta().getSaldo();
    }
}