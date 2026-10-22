package com.servicio.atm.app.service.impl;

import com.servicio.atm.app.service.DepositService;
import com.servicio.atm.domain.entity.Account;
import com.servicio.atm.domain.entity.Card;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.domain.exception.TarjetaNoEncontradaException;
import com.servicio.atm.infra.out.logging.TransactionLogger;
import com.servicio.atm.infra.out.persistence.AccountRepository;
import com.servicio.atm.infra.out.persistence.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Deposito de efectivo. Verifica que la tarjeta exista y este activa, bloquea
 * la cuenta destino (identificada por CBU, que puede ser de otra persona) y acredita
 * el importe dentro de una unica transaccion.
 * Cada intento, exitoso o rechazado, queda en el log transaccional.
 */
@Service
public class DepositServiceImpl implements DepositService {

    private static final String TIPO = "DEPOSITO";

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TransactionLogger transactionLogger;

    public DepositServiceImpl(CardRepository cardRepository,
                              AccountRepository accountRepository,
                              TransactionLogger transactionLogger) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.transactionLogger = transactionLogger;
    }

    @Override
    @Transactional
    public BigDecimal deposit(String cardNumber, String cbu, BigDecimal amount) {
        try {
            Card card = cardRepository.findByNumero(cardNumber)
                    .orElseThrow(TarjetaNoEncontradaException::new);

            if (!Boolean.TRUE.equals(card.getActiva())) {
                throw new TarjetaInactivaException();
            }

            // La cuenta destino se busca por CBU y puede ser de otra persona.
            Account cuenta = accountRepository.findByCbuForUpdate(cbu)
                    .orElseThrow(CuentaNoEncontradaException::new);

            cuenta.acreditar(amount);

            transactionLogger.registrar(TIPO, cardNumber, cbu, amount, "OK");
            return cuenta.getSaldo();
        } catch (RuntimeException e) {
            transactionLogger.registrar(TIPO, cardNumber, cbu, amount,
                    "RECHAZADA(" + e.getClass().getSimpleName() + ")");
            throw e;
        }
    }
}
