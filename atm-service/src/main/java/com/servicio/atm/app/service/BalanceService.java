package com.servicio.atm.app.service;

import java.math.BigDecimal;

/**
 * Caso de uso de consulta de saldo.
 */
public interface BalanceService {

    /**
     * Devuelve el saldo de una cuenta asociada a la tarjeta indicada.
     *
     * @param cardNumber    numero de tarjeta
     * @param accountNumber numero de cuenta
     * @return saldo actual de la cuenta
     * @throws com.servicio.atm.domain.exception.CuentaNoEncontradaException si la cuenta no existe o no corresponde a la tarjeta
     */
    BigDecimal getBalance(String cardNumber, String accountNumber);

}
