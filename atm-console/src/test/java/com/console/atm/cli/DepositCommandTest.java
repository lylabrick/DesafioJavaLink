package com.console.atm.cli;

import com.console.atm.cli.impl.DepositCommand;
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

public class DepositCommandTest {

    private AtmClient client;
    private ByteArrayOutputStream out;
    private DepositCommand command;

    @Before
    public void setUp() {
        client = mock(AtmClient.class);
        out = new ByteArrayOutputStream();
        command = new DepositCommand(client, new ConsolePrinter(new PrintStream(out), new PrintStream(out)));
    }

    @Test
    public void depositsToAnyCbuAndPrintsMessage() {
        BigDecimal amount = new BigDecimal("1000.50");
        when(client.deposit("1234", "CBU-AJENO", amount)).thenReturn(new TransactionResponse(amount, new BigDecimal("2000.50")));

        command.execute(new String[]{"1234", "CBU-AJENO", "1000,50"});

        verify(client).deposit("1234", "CBU-AJENO", amount);
        assertEquals("Depósito exitoso", out.toString().trim());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeAmountWithoutCallingService() {
        try {
            command.execute(new String[]{"1234", "CBU", "-10"});
        } finally {
            verifyNoInteractions(client);
        }
    }
}
