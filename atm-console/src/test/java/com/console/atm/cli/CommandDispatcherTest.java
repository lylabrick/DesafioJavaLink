package com.console.atm.cli;

import com.console.atm.cli.impl.BalanceCommand;
import com.console.atm.cli.impl.DepositCommand;
import com.console.atm.cli.impl.ExtractCommand;
import com.console.atm.cli.impl.LoginCommand;
import com.console.atm.client.AtmApiException;
import com.console.atm.client.AtmClient;
import com.console.atm.client.AtmUnavailableException;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandDispatcherTest {

    private AtmClient client;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private CommandDispatcher dispatcher;

    @Before
    public void setUp() {
        client = mock(AtmClient.class);
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        ConsolePrinter printer = new ConsolePrinter(new PrintStream(out), new PrintStream(err));
        dispatcher = new CommandDispatcher(Arrays.<Command>asList(
                new LoginCommand(client, printer),
                new ExtractCommand(client, printer),
                new DepositCommand(client, printer),
                new BalanceCommand(client, printer)), printer);
    }

    @Test
    public void returnsOkOnSuccess() {
        when(client.login("1234")).thenReturn(true);

        assertEquals(CommandDispatcher.EXIT_OK, dispatcher.dispatch(new String[]{"login", "1234"}));
        assertEquals("Ingreso exitoso", out.toString().trim());
    }

    @Test
    public void commandNameIsCaseInsensitive() {
        when(client.login("1234")).thenReturn(true);

        assertEquals(CommandDispatcher.EXIT_OK, dispatcher.dispatch(new String[]{"LOGIN", "1234"}));
    }

    @Test
    public void failedLoginPrintsMessageToStderrAndReturnsError() {
        when(client.login("1234")).thenReturn(false);

        assertEquals(CommandDispatcher.EXIT_ERROR, dispatcher.dispatch(new String[]{"login", "1234"}));
        assertEquals("Ingreso no exitoso", err.toString().trim());
    }

    @Test
    public void showsUsageWithoutArguments() {
        assertEquals(CommandDispatcher.EXIT_ERROR, dispatcher.dispatch(new String[]{}));
        assertTrue(err.toString().contains("atm depositar <tarjeta> <cbu> <importe>"));
    }

    @Test
    public void rejectsUnknownCommand() {
        assertEquals(CommandDispatcher.EXIT_ERROR, dispatcher.dispatch(new String[]{"transferir"}));
        assertTrue(err.toString().contains("Comando desconocido: transferir"));
    }

    @Test
    public void showsCommandUsageOnWrongArgumentCount() {
        assertEquals(CommandDispatcher.EXIT_ERROR, dispatcher.dispatch(new String[]{"saldo", "1234"}));
        assertTrue(err.toString().contains("Uso: atm saldo <tarjeta> <cuenta>"));
    }

    @Test
    public void showsServiceErrorMessage() {
        when(client.extract(any(String.class), any(String.class), any(BigDecimal.class)))
                .thenThrow(new AtmApiException(422, "Saldo insuficiente"));

        assertEquals(CommandDispatcher.EXIT_ERROR, dispatcher.dispatch(new String[]{"extraer", "1234", "ACC1", "9999"}));
        assertEquals("Saldo insuficiente", err.toString().trim());
    }

    @Test
    public void showsInvalidAmountMessage() {
        assertEquals(CommandDispatcher.EXIT_ERROR, dispatcher.dispatch(new String[]{"depositar", "1234", "CBU", "abc"}));
        assertTrue(err.toString().contains("Importe invalido"));
    }

    @Test
    public void showsFriendlyMessageWhenServiceIsDown() {
        when(client.getBalance("1234", "ACC1"))
                .thenThrow(new AtmUnavailableException("No se pudo conectar con el servicio ATM en http://localhost:8080", null));

        assertEquals(CommandDispatcher.EXIT_ERROR, dispatcher.dispatch(new String[]{"saldo", "1234", "ACC1"}));
        assertTrue(err.toString().contains("No se pudo conectar"));
        assertTrue(err.toString().contains("servicio este en ejecucion"));
    }
}
