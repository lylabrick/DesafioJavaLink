package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.DepositService;
import com.servicio.atm.app.service.ExtractService;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.SaldoInsuficienteException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.domain.exception.TarjetaNoEncontradaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private static final String EXTRACT_URL = "/api/transactions/extract";
    private static final String DEPOSIT_URL = "/api/transactions/deposit";

    private static final String EXTRACT_OK =
            "{\"cardNumber\":\"4000123412341234\",\"accountNumber\":\"0001\",\"amount\":100.00}";
    private static final String DEPOSIT_OK =
            "{\"cardNumber\":\"4000123412341234\",\"cbu\":\"0000000000000000000099\",\"amount\":250.50}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExtractService extractService;

    @MockBean
    private DepositService depositService;

    // ---------- extraccion ----------

    @Test
    @DisplayName("extract: 200 con el importe y el saldo resultante")
    void extractOk() throws Exception {
        when(extractService.extract(eq("4000123412341234"), eq("0001"), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("400.00"));

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(EXTRACT_OK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.balance").value(400.00));
    }

    @Test
    @DisplayName("extract: 400 con el detalle de cada campo faltante si el body esta vacio")
    void extractBodyVacio() throws Exception {
        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
                .andExpect(jsonPath("$.details.length()").value(3))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(extractService);
    }

    @Test
    @DisplayName("extract: 400 si el importe es cero")
    void extractImporteCero() throws Exception {
        String body = "{\"cardNumber\":\"4000123412341234\",\"accountNumber\":\"0001\",\"amount\":0}";

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("amount: El importe debe ser mayor que cero"));

        verifyNoInteractions(extractService);
    }

    @Test
    @DisplayName("extract: 400 si el importe tiene mas de dos decimales")
    void extractImporteConDemasiadosDecimales() throws Exception {
        String body = "{\"cardNumber\":\"4000123412341234\",\"accountNumber\":\"0001\",\"amount\":10.555}";

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("amount: Importe con formato invalido"));

        verifyNoInteractions(extractService);
    }

    @Test
    @DisplayName("extract: 400 si el importe no es un numero")
    void extractImporteNoNumerico() throws Exception {
        String body = "{\"cardNumber\":\"4000123412341234\",\"accountNumber\":\"0001\",\"amount\":\"abc\"}";

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud no es valido"));
    }

    @Test
    @DisplayName("extract: 400 si el JSON esta mal formado")
    void extractJsonMalFormado() throws Exception {
        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud no es valido"));
    }

    @Test
    @DisplayName("extract: 403 si la tarjeta esta inactiva")
    void extractTarjetaInactiva() throws Exception {
        when(extractService.extract(any(), any(), any())).thenThrow(new TarjetaInactivaException());

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(EXTRACT_OK))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("La tarjeta no esta activa"));
    }

    @Test
    @DisplayName("extract: 404 si la cuenta no existe o no corresponde a la tarjeta")
    void extractCuentaNoEncontrada() throws Exception {
        when(extractService.extract(any(), any(), any())).thenThrow(new CuentaNoEncontradaException());

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(EXTRACT_OK))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada para la tarjeta indicada"));
    }

    @Test
    @DisplayName("extract: 422 si el saldo es insuficiente")
    void extractSaldoInsuficiente() throws Exception {
        when(extractService.extract(any(), any(), any())).thenThrow(new SaldoInsuficienteException());

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(EXTRACT_OK))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Saldo insuficiente"))
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    @DisplayName("extract: 503 si no se pudo obtener el bloqueo de la cuenta")
    void extractServicioOcupado() throws Exception {
        when(extractService.extract(any(), any(), any())).thenThrow(new CannotAcquireLockException("lock"));

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(EXTRACT_OK))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("El servicio esta ocupado, intente nuevamente"));
    }

    @Test
    @DisplayName("extract: 500 generico sin filtrar el mensaje interno si ocurre un error inesperado")
    void extractErrorInesperado() throws Exception {
        when(extractService.extract(any(), any(), any())).thenThrow(new RuntimeException("detalle interno"));

        mockMvc.perform(post(EXTRACT_URL).contentType(MediaType.APPLICATION_JSON).content(EXTRACT_OK))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error interno del servicio"));
    }

    // ---------- deposito ----------

    @Test
    @DisplayName("deposit: 200 con el importe y el saldo resultante")
    void depositOk() throws Exception {
        when(depositService.deposit(eq("4000123412341234"), eq("0000000000000000000099"), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("350.50"));

        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content(DEPOSIT_OK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(250.50))
                .andExpect(jsonPath("$.balance").value(350.50));
    }

    @Test
    @DisplayName("deposit: 400 con el detalle de cada campo faltante si el body esta vacio")
    void depositBodyVacio() throws Exception {
        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
                .andExpect(jsonPath("$.details.length()").value(3));

        verifyNoInteractions(depositService);
    }

    @Test
    @DisplayName("deposit: 400 si el importe es negativo")
    void depositImporteNegativo() throws Exception {
        String body = "{\"cardNumber\":\"4000123412341234\",\"cbu\":\"0000000000000000000099\",\"amount\":-5}";

        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("amount: El importe debe ser mayor que cero"));

        verifyNoInteractions(depositService);
    }

    @Test
    @DisplayName("deposit: 400 si el servicio rechaza el importe con IllegalArgumentException")
    void depositImporteInvalidoEnDominio() throws Exception {
        when(depositService.deposit(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("El importe debe ser mayor que cero"));

        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content(DEPOSIT_OK))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El importe debe ser mayor que cero"));
    }

    @Test
    @DisplayName("deposit: 403 si la tarjeta esta inactiva")
    void depositTarjetaInactiva() throws Exception {
        when(depositService.deposit(any(), any(), any())).thenThrow(new TarjetaInactivaException());

        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content(DEPOSIT_OK))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("La tarjeta no esta activa"));
    }

    @Test
    @DisplayName("deposit: 404 si la tarjeta no existe")
    void depositTarjetaInexistente() throws Exception {
        when(depositService.deposit(any(), any(), any())).thenThrow(new TarjetaNoEncontradaException());

        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content(DEPOSIT_OK))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Tarjeta no encontrada"));
    }

    @Test
    @DisplayName("deposit: 404 si no existe una cuenta con ese CBU")
    void depositCbuInexistente() throws Exception {
        when(depositService.deposit(any(), any(), any())).thenThrow(new CuentaNoEncontradaException());

        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content(DEPOSIT_OK))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("deposit: 503 si no se pudo obtener el bloqueo de la cuenta")
    void depositServicioOcupado() throws Exception {
        when(depositService.deposit(any(), any(), any())).thenThrow(new CannotAcquireLockException("lock"));

        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.APPLICATION_JSON).content(DEPOSIT_OK))
                .andExpect(status().isServiceUnavailable());
    }

    // ---------- errores de protocolo (GlobalExceptionHandler) ----------

    @Test
    @DisplayName("405: un GET sobre un endpoint que solo acepta POST")
    void metodoNoPermitido() throws Exception {
        mockMvc.perform(get(DEPOSIT_URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Metodo HTTP no permitido para este recurso"));
    }

    @Test
    @DisplayName("415: un POST con un Content-Type que no es JSON")
    void contentTypeNoSoportado() throws Exception {
        mockMvc.perform(post(DEPOSIT_URL).contentType(MediaType.TEXT_PLAIN).content(DEPOSIT_OK))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").value("Content-Type no soportado, se espera application/json"));
    }
}
