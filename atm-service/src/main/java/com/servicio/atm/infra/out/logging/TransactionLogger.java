package com.servicio.atm.infra.out.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Escribe una linea por operacion en el log transaccional (logs/transacciones.log).
 */
@Component
public class TransactionLogger {

    private static final Logger log = LoggerFactory.getLogger("TRANSACCIONES");

    public void registrar(String tipo, String cardNumber, String accountNumber,
                          BigDecimal importe, String resultado) {
        log.info("tipo={} tarjeta={} cuenta={} importe={} resultado={}",
                tipo, enmascarar(cardNumber), accountNumber, importe, resultado);
    }

    private String enmascarar(String numero) {
        if (numero == null || numero.length() <= 4) {
            return "****";
        }
        return "****" + numero.substring(numero.length() - 4);
    }
}