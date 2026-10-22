package com.console.atm.cli.impl;

import com.console.atm.cli.Command;
import com.console.atm.cli.ConsolePrinter;
import com.console.atm.client.AtmClient;
import com.console.atm.util.AmountFormatter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class BalanceCommand implements Command {

    private final AtmClient client;
    private final ConsolePrinter printer;

    @Override
    public String getName() {
        return "saldo";
    }

    @Override
    public String getUsage() {
        return "saldo <tarjeta> <cuenta>";
    }

    @Override
    public int getArgCount() {
        return 2;
    }

    @Override
    public void execute(String[] args) {
        BigDecimal balance = client.getBalance(args[0], args[1]);
        printer.info("Su saldo es $ " + AmountFormatter.format(balance));
    }
}
