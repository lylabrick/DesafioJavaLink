package com.servicio.atm.domain.exception;

/**
 * Se lanza cuando el numero de tarjeta indicado no existe.
 */
public class TarjetaNoEncontradaException extends RuntimeException {

    public TarjetaNoEncontradaException() {
        super("Tarjeta no encontrada");
    }
}
