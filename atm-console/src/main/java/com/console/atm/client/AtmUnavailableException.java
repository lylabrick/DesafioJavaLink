package com.console.atm.client;

/** No se pudo comunicar con el servicio (sin conexion, timeout o respuesta ilegible). */
public class AtmUnavailableException extends RuntimeException {

    public AtmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
