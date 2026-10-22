package com.servicio.atm.app.service;

/**
 * Caso de uso de identificacion con tarjeta.
 */
public interface LoginService {

    /**
     * Indica si una tarjeta puede ingresar al sistema. El intento queda registrado
     * en el log transaccional.
     *
     * @param cardNumber numero de tarjeta
     * @return {@code true} si la tarjeta existe y esta activa; {@code false} en caso contrario
     */
    Boolean login(String cardNumber);

}
