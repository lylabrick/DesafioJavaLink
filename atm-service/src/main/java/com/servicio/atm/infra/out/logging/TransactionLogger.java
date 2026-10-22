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

    /**
     * Registra una operacion. El numero de tarjeta se enmascara y solo se
     * conservan los ultimos cuatro digitos.
     *
     * @param tipo          tipo de operacion (por ejemplo EXTRACCION o DEPOSITO)
     * @param cardNumber    numero de tarjeta
     * @param accountNumber cuenta o CBU afectado
     * @param importe       importe de la operacion
     * @param resultado     resultado de la operacion
     */
    public void registrar(String tipo, String cardNumber, String accountNumber,
                          BigDecimal importe, String resultado) {
        log.info("tipo={} tarjeta={} cuenta={} importe={} resultado={}",
                tipo, enmascarar(cardNumber), accountNumber, importe, resultado);
    }

    /**
     * Registra un intento de identificacion. El numero de tarjeta se enmascara y
     * solo se conservan los ultimos cuatro digitos.
     *
     * @param cardNumber numero de tarjeta
     * @param exitoso    {@code true} si el ingreso fue exitoso; {@code false} en caso contrario
     */
    public void registrarLogin(String cardNumber, boolean exitoso) {
        log.info("tipo={} tarjeta={} resultado={}",
                "LOGIN", enmascarar(cardNumber), exitoso ? "EXITOSO" : "NO_EXITOSO");
    }

    private String enmascarar(String numero) {
        if (numero == null || numero.length() <= 4) {
            return "****";
        }
        return "****" + numero.substring(numero.length() - 4);
    }
}