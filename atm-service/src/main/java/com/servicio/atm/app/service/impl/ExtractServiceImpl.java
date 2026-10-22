package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.ExtractService;
import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.CardAccount;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.SaldoInsuficienteException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.infra.out.logging.TransactionLogger;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Extraccion de efectivo. Valida la asociacion tarjeta-cuenta y que la tarjeta este activa, bloquea la cuenta
 * (bloqueo pesimista) y debita el importe dentro de una unica transaccion, de modo
 * que operaciones concurrentes sobre la misma cuenta se ejecutan en serie.
 * Cada intento, exitoso o rechazado, queda en el log transaccional.
 */
@Service
public class ExtractServiceImpl implements ExtractService {

    private static final String TIPO = "EXTRACCION";

    private final CardAccountRepository cardAccountRepository;
    private final AccountRepository accountRepository;
    private final TransactionLogger transactionLogger;

    public ExtractServiceImpl(CardAccountRepository cardAccountRepository,
                              AccountRepository accountRepository,
                              TransactionLogger transactionLogger) {
        this.cardAccountRepository = cardAccountRepository;
        this.accountRepository = accountRepository;
        this.transactionLogger = transactionLogger;
    }

    @Override
    @Transactional
    public BigDecimal extract(String cardNumber, String accountNumber, BigDecimal amount) {
        try {
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

            transactionLogger.registrar(TIPO, cardNumber, accountNumber, amount, "OK");
            return cuenta.getSaldo();
        } catch (RuntimeException e) {
            transactionLogger.registrar(TIPO, cardNumber, accountNumber, amount,
                    "RECHAZADA(" + e.getClass().getSimpleName() + ")");
            throw e;
        }
    }
}
