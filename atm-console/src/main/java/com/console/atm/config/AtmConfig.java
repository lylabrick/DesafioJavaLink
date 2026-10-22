package com.console.atm.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Configuracion de conexion al servicio. La URL base se puede cambiar con la variable ATM_BASE_URL. */
@Getter
@AllArgsConstructor
public class AtmConfig {

    public static final String BASE_URL_ENV = "ATM_BASE_URL";
    public static final String DEFAULT_BASE_URL = "http://localhost:8080";
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;

    private final String baseUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public static AtmConfig fromEnvironment() {
        String url = System.getenv(BASE_URL_ENV);
        if (url == null || url.trim().isEmpty()) {
            url = DEFAULT_BASE_URL;
        }
        return new AtmConfig(stripTrailingSlash(url.trim()), DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
