package com.console.atm.cli;

import com.console.atm.cli.impl.LoginCommand;
import com.console.atm.client.AtmApiException;
import com.console.atm.client.AtmClient;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoginCommandTest {

    private AtmClient client;
    private ByteArrayOutputStream out;
    private LoginCommand command;

    @Before
    public void setUp() {
        client = mock(AtmClient.class);
        out = new ByteArrayOutputStream();
        command = new LoginCommand(client, new ConsolePrinter(new PrintStream(out), new PrintStream(out)));
    }

    @Test
    public void printsSuccessWhenLoginSucceeds() {
        when(client.login("1234")).thenReturn(true);

        command.execute(new String[]{"1234"});

        assertEquals("Ingreso exitoso", out.toString().trim());
    }

    @Test
    public void failsWhenServiceReportsFailure() {
        when(client.login("1234")).thenReturn(false);

        assertFails("Ingreso no exitoso");
    }

    @Test
    public void failsWhenCardIsInactive() {
        when(client.login("1234")).thenThrow(new AtmApiException(403, "Tarjeta inactiva"));

        assertFails("Ingreso no exitoso");
    }

    @Test(expected = AtmApiException.class)
    public void propagatesOtherServiceErrors() {
        when(client.login("1234")).thenThrow(new AtmApiException(500, "Error interno"));

        command.execute(new String[]{"1234"});
    }

    private void assertFails(String expectedMessage) {
        try {
            command.execute(new String[]{"1234"});
            fail("Se esperaba CommandFailedException");
        } catch (CommandFailedException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }
}
