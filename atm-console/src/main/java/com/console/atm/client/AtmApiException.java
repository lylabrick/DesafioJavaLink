package com.console.atm.client;

import lombok.Getter;

/** El servicio respondio con un error HTTP (4xx/5xx). */
@Getter
public class AtmApiException extends RuntimeException {

    private final int status;

    public AtmApiException(int status, String message) {
        super(message);
        this.status = status;
    }
}
