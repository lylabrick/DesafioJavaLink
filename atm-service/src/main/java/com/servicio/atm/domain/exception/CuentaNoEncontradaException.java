package com.servicio.atm.domain.exception;

/**
 * Se lanza cuando no existe la relacion entre la tarjeta y la cuenta indicadas.
 */
public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException() {
        super("Cuenta no encontrada para la tarjeta indicada");
    }
}