package com.console.atm.client;

import com.console.atm.config.AtmConfig;
import com.console.atm.dto.BalanceRequest;
import com.console.atm.dto.BalanceResponse;
import com.console.atm.dto.DepositRequest;
import com.console.atm.dto.ErrorResponse;
import com.console.atm.dto.ExtractRequest;
import com.console.atm.dto.LoginRequest;
import com.console.atm.dto.LoginResponse;
import com.console.atm.dto.TransactionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class HttpAtmClient implements AtmClient {

    private static final String LOGIN_PATH = "/api/login";
    private static final String BALANCE_PATH = "/api/accounts/balance";
    private static final String EXTRACT_PATH = "/api/transactions/extract";
    private static final String DEPOSIT_PATH = "/api/transactions/deposit";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AtmConfig config;

    @Override
    public boolean login(String cardNumber) {
        return post(LOGIN_PATH, new LoginRequest(cardNumber), LoginResponse.class).isSuccess();
    }

    @Override
    public BigDecimal getBalance(String cardNumber, String accountNumber) {
        return post(BALANCE_PATH, new BalanceRequest(cardNumber, accountNumber), BalanceResponse.class).getBalance();
    }

    @Override
    public TransactionResponse extract(String cardNumber, String accountNumber, BigDecimal amount) {
        return post(EXTRACT_PATH, new ExtractRequest(cardNumber, accountNumber, amount), TransactionResponse.class);
    }

    @Override
    public TransactionResponse deposit(String cardNumber, String cbu, BigDecimal amount) {
        return post(DEPOSIT_PATH, new DepositRequest(cardNumber, cbu, amount), TransactionResponse.class);
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(config.getBaseUrl() + path).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(config.getConnectTimeoutMs());
            connection.setReadTimeout(config.getReadTimeoutMs());
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            try (OutputStream out = connection.getOutputStream()) {
                out.write(MAPPER.writeValueAsBytes(body));
            }

            int status = connection.getResponseCode();
            if (status >= 200 && status < 300) {
                try (InputStream in = connection.getInputStream()) {
                    return MAPPER.readValue(in, responseType);
                }
            }
            throw toApiException(status, connection.getErrorStream());
        } catch (JsonProcessingException e) {
            throw new AtmUnavailableException("Respuesta inesperada del servicio ATM", e);
        } catch (IOException e) {
            throw new AtmUnavailableException(
                    "No se pudo conectar con el servicio ATM en " + config.getBaseUrl(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /** Arma la excepcion con el mensaje del servicio; si el cuerpo no es el esperado usa uno generico. */
    private AtmApiException toApiException(int status, InputStream errorStream) {
        String message = "El servicio respondio con error HTTP " + status;
        if (errorStream != null) {
            try (InputStream in = errorStream) {
                ErrorResponse error = MAPPER.readValue(in, ErrorResponse.class);
                if (error.getMessage() != null && !error.getMessage().isEmpty()) {
                    message = error.getMessage();
                    if (error.getDetails() != null && !error.getDetails().isEmpty()) {
                        message += ": " + String.join("; ", error.getDetails());
                    }
                }
            } catch (IOException ignored) {
                // cuerpo vacio o no JSON: se mantiene el mensaje generico
            }
        }
        return new AtmApiException(status, message);
    }
}
