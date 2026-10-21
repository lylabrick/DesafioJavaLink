package com.servicio.atm.app.service;

import java.math.BigDecimal;

public interface ExtractService {

    BigDecimal extract(String cardNumber, String accountNumber, BigDecimal amount);

}