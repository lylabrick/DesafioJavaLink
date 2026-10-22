package com.console.atm.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Configuracion de conexion al servicio, que siempre corre en local. */
@Getter
@AllArgsConstructor
public class AtmConfig {

    public static final String BASE_URL = "http://localhost:8080";
    public static final int CONNECT_TIMEOUT_MS = 3000;
    public static final int READ_TIMEOUT_MS = 10000;

    private final String baseUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public static AtmConfig defaults() {
        return new AtmConfig(BASE_URL, CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
    }
}
