package com.servicio.atm.domain.exception;

/**
 * Se lanza cuando la tarjeta existe pero no esta habilitada para operar.
 */
public class TarjetaInactivaException extends RuntimeException {

    public TarjetaInactivaException() {
        super("La tarjeta no esta activa");
    }
}