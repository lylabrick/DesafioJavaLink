package com.servicio.atm.domain.model.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {
    private BigDecimal amount;
    private BigDecimal balance;
}