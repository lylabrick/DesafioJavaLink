package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.BalanceService;
import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final String URL = "/api/accounts/balance";
    private static final String BODY_OK = "{\"cardNumber\":\"4000123412341234\",\"accountNumber\":\"0001\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BalanceService balanceService;

    @Test
    @DisplayName("balance: 200 con el saldo de la cuenta")
    void saldoOk() throws Exception {
        when(balanceService.getBalance("4000123412341234", "0001")).thenReturn(new BigDecimal("1234.56"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_OK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1234.56));
    }

    @Test
    @DisplayName("balance: 400 si faltan datos")
    void bodyVacio() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.length()").value(2));

        verifyNoInteractions(balanceService);
    }

    @Test
    @DisplayName("balance: 404 si la cuenta no existe o no corresponde a la tarjeta")
    void cuentaNoEncontrada() throws Exception {
        when(balanceService.getBalance("4000123412341234", "0001")).thenThrow(new CuentaNoEncontradaException());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_OK))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada para la tarjeta indicada"));
    }
}
