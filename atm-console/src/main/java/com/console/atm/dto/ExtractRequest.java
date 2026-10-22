package com.console.atm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractRequest {
    private String cardNumber;
    private String accountNumber;
    private BigDecimal amount;
}
