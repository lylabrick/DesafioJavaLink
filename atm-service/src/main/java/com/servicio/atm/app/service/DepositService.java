package com.servicio.atm.app.service;

import java.math.BigDecimal;

/**
 * Caso de uso de deposito de efectivo.
 */
public interface DepositService {

    /**
     * Acredita un importe en la cuenta identificada por su CBU. La cuenta puede
     * pertenecer o no al titular de la tarjeta.
     *
     * @param cardNumber numero de la tarjeta con la que se opera; debe existir y estar activa
     * @param cbu        CBU de la cuenta destino
     * @param amount     importe a acreditar, mayor que cero
     * @return saldo de la cuenta luego del deposito
     * @throws com.servicio.atm.domain.exception.TarjetaNoEncontradaException si la tarjeta no existe
     * @throws com.servicio.atm.domain.exception.TarjetaInactivaException     si la tarjeta esta inactiva
     * @throws com.servicio.atm.domain.exception.CuentaNoEncontradaException  si no existe una cuenta con ese CBU
     */
    BigDecimal deposit(String cardNumber, String cbu, BigDecimal amount);

}