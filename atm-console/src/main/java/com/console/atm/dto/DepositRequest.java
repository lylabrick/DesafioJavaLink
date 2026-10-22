package com.console.atm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** La cuenta destino se identifica por CBU y puede no pertenecer al titular de la tarjeta. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    private String cardNumber;
    private String cbu;
    private BigDecimal amount;
}
