package com.servicio.atm.domain.model.in;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class BalanceRequest {

    @NotBlank(message = "El numero de tarjeta es obligatorio")
    private String cardNumber;

    @NotBlank(message = "El numero de cuenta es obligatorio")
    private String accountNumber;

}