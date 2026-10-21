package com.servicio.atm.domain.exception;

/**
 * Se lanza cuando el importe solicitado supera el saldo disponible.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException() {
        super("Saldo insuficiente");
    }
}