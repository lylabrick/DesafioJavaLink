package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.LoginService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    private static final String URL = "/api/login";
    private static final String BODY_OK = "{\"cardNumber\":\"4000123412341234\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginService loginService;

    @Test
    @DisplayName("login: 200 con success=true si la tarjeta es valida")
    void ingresoExitoso() throws Exception {
        when(loginService.login("4000123412341234")).thenReturn(true);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_OK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("login: 200 con success=false si la tarjeta no es valida")
    void ingresoNoExitoso() throws Exception {
        when(loginService.login("4000123412341234")).thenReturn(false);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_OK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("login: 400 si falta el numero de tarjeta")
    void sinTarjeta() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("cardNumber: El numero de tarjeta es obligatorio"));

        verifyNoInteractions(loginService);
    }
}
