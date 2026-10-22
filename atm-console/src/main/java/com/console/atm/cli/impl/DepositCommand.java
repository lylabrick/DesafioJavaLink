package com.console.atm.cli.impl;

import com.console.atm.cli.Command;
import com.console.atm.cli.ConsolePrinter;
import com.console.atm.client.AtmClient;
import com.console.atm.util.AmountParser;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class DepositCommand implements Command {

    private final AtmClient client;
    private final ConsolePrinter printer;

    @Override
    public String getName() {
        return "depositar";
    }

    @Override
    public String getUsage() {
        return "depositar <tarjeta> <cbu> <importe>";
    }

    @Override
    public int getArgCount() {
        return 3;
    }

    @Override
    public void execute(String[] args) {
        BigDecimal amount = AmountParser.parse(args[2]);
        client.deposit(args[0], args[1], amount);
        printer.info("Depósito exitoso");
    }
}
