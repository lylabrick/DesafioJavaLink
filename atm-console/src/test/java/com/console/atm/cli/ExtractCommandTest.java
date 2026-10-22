package com.console.atm.cli;

import com.console.atm.client.AtmClient;
import com.console.atm.dto.TransactionResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ExtractCommandTest {

    private AtmClient client;
    private ByteArrayOutputStream out;
    private ExtractCommand command;

    @Before
    public void setUp() {
        client = mock(AtmClient.class);
        out = new ByteArrayOutputStream();
        command = new ExtractCommand(client, new ConsolePrinter(new PrintStream(out), new PrintStream(out)));
    }

    @Test
    public void callsServiceAndPrintsMessage() {
        BigDecimal amount = new BigDecimal("500.00");
        when(client.extract("1234", "ACC1", amount)).thenReturn(new TransactionResponse(amount, new BigDecimal("1000.00")));

        command.execute(new String[]{"1234", "ACC1", "500"});

        verify(client).extract("1234", "ACC1", amount);
        assertEquals("Retire su dinero", out.toString().trim());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidAmountWithoutCallingService() {
        try {
            command.execute(new String[]{"1234", "ACC1", "abc"});
        } finally {
            verifyNoInteractions(client);
        }
    }
}
