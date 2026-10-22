package com.servicio.atm.app.service;

import java.math.BigDecimal;

/**
 * Caso de uso de extraccion de efectivo.
 */
public interface ExtractService {

    /**
     * Debita un importe de una cuenta asociada a la tarjeta indicada. El saldo
     * no puede quedar negativo.
     *
     * @param cardNumber    numero de tarjeta
     * @param accountNumber numero de cuenta
     * @param amount        importe a retirar, mayor que cero
     * @return saldo de la cuenta luego de la extraccion
     * @throws com.servicio.atm.domain.exception.CuentaNoEncontradaException si la cuenta no existe o no corresponde a la tarjeta
     * @throws com.servicio.atm.domain.exception.TarjetaInactivaException    si la tarjeta esta inactiva
     * @throws com.servicio.atm.domain.exception.SaldoInsuficienteException  si el saldo no alcanza para el importe
     */
    BigDecimal extract(String cardNumber, String accountNumber, BigDecimal amount);

}