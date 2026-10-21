package com.servicio.atm.app.service;

import java.math.BigDecimal;

public interface BalanceService {

    BigDecimal getBalance(String cardNumber, String accountNumber);

}
