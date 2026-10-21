package com.servicio.atm.domain.model.in;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DepositRequest {

    @NotBlank(message = "El numero de tarjeta es obligatorio")
    private String cardNumber;

    @NotBlank(message = "El CBU es obligatorio")
    private String cbu;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor que cero")
    @Digits(integer = 13, fraction = 2, message = "Importe con formato invalido")
    private BigDecimal amount;
}