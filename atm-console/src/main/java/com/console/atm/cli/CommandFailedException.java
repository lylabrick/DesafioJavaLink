package com.console.atm.cli;

/** El comando termino sin exito por una razon propia de la operacion; el mensaje se muestra al usuario. */
public class CommandFailedException extends RuntimeException {

    public CommandFailedException(String message) {
        super(message);
    }
}
