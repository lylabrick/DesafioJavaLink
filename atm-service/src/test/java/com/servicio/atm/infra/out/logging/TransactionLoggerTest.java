package com.servicio.atm.infra.out.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TransactionLoggerTest {

    private final TransactionLogger transactionLogger = new TransactionLogger();

    private Logger logger;
    private Level nivelOriginal;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger("TRANSACCIONES");
        nivelOriginal = logger.getLevel();
        logger.setLevel(Level.INFO);

        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        logger.setLevel(nivelOriginal);
    }

    private List<ILoggingEvent> eventos() {
        return appender.list;
    }

    @Test
    @DisplayName("registrar escribe una linea con tipo, tarjeta enmascarada, cuenta, importe y resultado")
    void registrarEscribeLaLinea() {
        transactionLogger.registrar("EXTRACCION", "4000123412341234", "0001", new BigDecimal("100.00"), "OK");

        assertEquals(1, eventos().size());
        assertEquals("tipo=EXTRACCION tarjeta=****1234 cuenta=0001 importe=100.00 resultado=OK",
                eventos().get(0).getFormattedMessage());
        assertEquals(Level.INFO, eventos().get(0).getLevel());
    }

    @Test
    @DisplayName("registrar escribe el resultado de un rechazo")
    void registrarEscribeUnRechazo() {
        transactionLogger.registrar("DEPOSITO", "4000123412341234", "0000000000000000000099",
                new BigDecimal("10.50"), "RECHAZADA(CuentaNoEncontradaException)");

        assertEquals("tipo=DEPOSITO tarjeta=****1234 cuenta=0000000000000000000099 importe=10.50 "
                        + "resultado=RECHAZADA(CuentaNoEncontradaException)",
                eventos().get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("registrarLogin escribe EXITOSO para un ingreso exitoso")
    void registrarLoginExitoso() {
        transactionLogger.registrarLogin("4000123412341234", true);

        assertEquals(1, eventos().size());
        assertEquals("tipo=LOGIN tarjeta=****1234 resultado=EXITOSO",
                eventos().get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("registrarLogin escribe NO_EXITOSO para un ingreso rechazado")
    void registrarLoginNoExitoso() {
        transactionLogger.registrarLogin("4000123412341234", false);

        assertEquals("tipo=LOGIN tarjeta=****1234 resultado=NO_EXITOSO",
                eventos().get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("el numero de tarjeta completo nunca aparece en el log")
    void laTarjetaNuncaApareceCompleta() {
        String tarjeta = "4000123412341234";

        transactionLogger.registrar("EXTRACCION", tarjeta, "0001", BigDecimal.TEN, "OK");
        transactionLogger.registrarLogin(tarjeta, true);

        for (ILoggingEvent evento : eventos()) {
            assertFalse(evento.getFormattedMessage().contains(tarjeta));
        }
    }

    @Test
    @DisplayName("una tarjeta de cuatro digitos o menos se enmascara por completo")
    void tarjetaCortaSeEnmascaraPorCompleto() {
        transactionLogger.registrarLogin("1234", true);
        transactionLogger.registrarLogin("12", true);

        assertEquals("tipo=LOGIN tarjeta=**** resultado=EXITOSO", eventos().get(0).getFormattedMessage());
        assertEquals("tipo=LOGIN tarjeta=**** resultado=EXITOSO", eventos().get(1).getFormattedMessage());
    }

    @Test
    @DisplayName("una tarjeta nula se enmascara por completo")
    void tarjetaNulaSeEnmascara() {
        transactionLogger.registrarLogin(null, false);

        assertEquals("tipo=LOGIN tarjeta=**** resultado=NO_EXITOSO", eventos().get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("una tarjeta de cinco digitos conserva solo los ultimos cuatro")
    void tarjetaDeCincoDigitos() {
        transactionLogger.registrarLogin("12345", true);

        assertEquals("tipo=LOGIN tarjeta=****2345 resultado=EXITOSO", eventos().get(0).getFormattedMessage());
    }
}
