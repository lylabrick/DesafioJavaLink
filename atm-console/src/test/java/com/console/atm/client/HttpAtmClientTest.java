package com.console.atm.client;

import com.console.atm.config.AtmConfig;
import com.console.atm.dto.TransactionResponse;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpAtmClientTest {

    private HttpServer server;
    private HttpAtmClient client;
    private volatile String lastMethod;
    private volatile String lastPath;
    private volatile String lastBody;

    @Before
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        client = clientFor(server.getAddress().getPort());
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void loginSendsCardAndReadsSuccess() {
        respond("/api/login", 200, "{\"success\":true}");

        assertTrue(client.login("1234"));
        assertEquals("POST", lastMethod);
        assertEquals("/api/login", lastPath);
        assertEquals("{\"cardNumber\":\"1234\"}", lastBody);
    }

    @Test
    public void loginReturnsFalseWhenServiceSaysSo() {
        respond("/api/login", 200, "{\"success\":false}");

        assertFalse(client.login("0000"));
    }

    @Test
    public void getBalanceReadsBalance() {
        respond("/api/accounts/balance", 200, "{\"balance\":1500.50}");

        assertEquals(new BigDecimal("1500.50"), client.getBalance("1234", "ACC1"));
        assertEquals("{\"cardNumber\":\"1234\",\"accountNumber\":\"ACC1\"}", lastBody);
    }

    @Test
    public void extractSendsAmountAndReadsResponse() {
        respond("/api/transactions/extract", 200, "{\"amount\":500.00,\"balance\":1000.00}");

        TransactionResponse response = client.extract("1234", "ACC1", new BigDecimal("500.00"));

        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertTrue(lastBody.contains("\"accountNumber\":\"ACC1\""));
        assertTrue(lastBody.contains("\"amount\":500.00"));
    }

    @Test
    public void depositUsesCbuField() {
        respond("/api/transactions/deposit", 200, "{\"amount\":100.00,\"balance\":600.00}");

        client.deposit("1234", "CBU-1", new BigDecimal("100.00"));

        assertTrue(lastBody.contains("\"cbu\":\"CBU-1\""));
        assertFalse(lastBody.contains("accountNumber"));
    }

    @Test
    public void mapsErrorResponseToApiException() {
        respond("/api/transactions/extract", 422,
                "{\"status\":422,\"error\":\"Unprocessable Entity\",\"message\":\"Saldo insuficiente\",\"details\":[],\"timestamp\":\"2026-01-01T10:00:00\"}");

        try {
            client.extract("1234", "ACC1", new BigDecimal("999999"));
        } catch (AtmApiException e) {
            assertEquals(422, e.getStatus());
            assertEquals("Saldo insuficiente", e.getMessage());
            return;
        }
        throw new AssertionError("Se esperaba AtmApiException");
    }

    @Test
    public void includesValidationDetailsInMessage() {
        respond("/api/login", 400,
                "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Datos de entrada invalidos\",\"details\":[\"cardNumber: obligatorio\"]}");

        try {
            client.login("");
        } catch (AtmApiException e) {
            assertEquals("Datos de entrada invalidos: cardNumber: obligatorio", e.getMessage());
            return;
        }
        throw new AssertionError("Se esperaba AtmApiException");
    }

    @Test
    public void usesGenericMessageWhenErrorBodyIsNotJson() {
        respond("/api/login", 502, "<html>Bad gateway</html>");

        try {
            client.login("1234");
        } catch (AtmApiException e) {
            assertEquals(502, e.getStatus());
            assertEquals("El servicio respondio con error HTTP 502", e.getMessage());
            return;
        }
        throw new AssertionError("Se esperaba AtmApiException");
    }

    @Test(expected = AtmUnavailableException.class)
    public void reportsUnavailableWhenNothingIsListening() {
        int closedPort = server.getAddress().getPort();
        server.stop(0);

        clientFor(closedPort).login("1234");
    }

    @Test(expected = AtmUnavailableException.class)
    public void reportsUnavailableWhenResponseIsNotJson() {
        respond("/api/login", 200, "no es json");

        client.login("1234");
    }

    private HttpAtmClient clientFor(int port) {
        return new HttpAtmClient(new AtmConfig("http://127.0.0.1:" + port, 1000, 2000));
    }

    private void respond(String path, int status, String json) {
        server.createContext(path, exchange -> {
            lastMethod = exchange.getRequestMethod();
            lastPath = exchange.getRequestURI().getPath();
            lastBody = read(exchange.getRequestBody());
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }

    private static String read(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
