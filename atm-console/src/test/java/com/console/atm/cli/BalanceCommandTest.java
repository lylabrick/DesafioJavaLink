package com.console.atm.cli;

import com.console.atm.cli.impl.BalanceCommand;
import com.console.atm.client.AtmClient;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BalanceCommandTest {

    @Test
    public void printsBalanceWithCommaDecimal() {
        AtmClient client = mock(AtmClient.class);
        when(client.getBalance("1234", "ACC1")).thenReturn(new BigDecimal("99999.99"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BalanceCommand command = new BalanceCommand(client, new ConsolePrinter(new PrintStream(out), new PrintStream(out)));

        command.execute(new String[]{"1234", "ACC1"});

        assertEquals("Su saldo es $ 99999,99", out.toString().trim());
    }
}
