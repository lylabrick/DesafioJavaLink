package com.servicio.atm.app.service;

import java.math.BigDecimal;

public interface DepositService {

    BigDecimal deposit(String cardNumber, String accountNumber, BigDecimal amount);

}