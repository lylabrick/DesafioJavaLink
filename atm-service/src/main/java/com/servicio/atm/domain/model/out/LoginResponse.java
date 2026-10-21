package com.servicio.atm.domain.model.out;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private final boolean success;
}