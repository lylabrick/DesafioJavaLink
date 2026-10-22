package com.console.atm.cli;

import com.console.atm.client.AtmClient;
import com.console.atm.util.AmountParser;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ExtractCommand implements Command {

    private final AtmClient client;
    private final ConsolePrinter printer;

    @Override
    public String getName() {
        return "extraer";
    }

    @Override
    public String getUsage() {
        return "extraer <tarjeta> <cuenta> <importe>";
    }

    @Override
    public int getArgCount() {
        return 3;
    }

    @Override
    public void execute(String[] args) {
        BigDecimal amount = AmountParser.parse(args[2]);
        client.extract(args[0], args[1], amount);
        printer.info("Retire su dinero");
    }
}
