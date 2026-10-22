package com.console.atm.client;

import com.console.atm.dto.TransactionResponse;

import java.math.BigDecimal;

/** Operaciones del servicio ATM. Los errores se informan con AtmApiException o AtmUnavailableException. */
public interface AtmClient {

    /** @return true si la tarjeta existe y esta activa */
    boolean login(String cardNumber);

    BigDecimal getBalance(String cardNumber, String accountNumber);

    TransactionResponse extract(String cardNumber, String accountNumber, BigDecimal amount);

    /** Acredita el importe en la cuenta identificada por el CBU, que puede ser de otra persona. */
    TransactionResponse deposit(String cardNumber, String cbu, BigDecimal amount);
}
