package com.servicio.atm.domain.model.out;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Respuesta de la identificacion: {@code success} indica si el ingreso fue exitoso.
 */
@Getter
@AllArgsConstructor
public class LoginResponse {

    private final boolean success;
}