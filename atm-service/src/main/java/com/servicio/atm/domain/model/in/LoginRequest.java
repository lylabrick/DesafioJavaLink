package com.servicio.atm.domain.model.in;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El numero de tarjeta es obligatorio")
    private String cardNumber;
}