package com.console.atm.cli.impl;

import com.console.atm.cli.Command;
import com.console.atm.cli.CommandFailedException;
import com.console.atm.cli.ConsolePrinter;
import com.console.atm.client.AtmApiException;
import com.console.atm.client.AtmClient;
import lombok.RequiredArgsConstructor;

import java.net.HttpURLConnection;

@RequiredArgsConstructor
public class LoginCommand implements Command {

    private final AtmClient client;
    private final ConsolePrinter printer;

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public String getUsage() {
        return "login <tarjeta>";
    }

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public void execute(String[] args) {
        boolean success;
        try {
            success = client.login(args[0]);
        } catch (AtmApiException e) {
            // tarjeta inactiva: el servicio responde 403, que para el usuario es un ingreso fallido
            if (e.getStatus() != HttpURLConnection.HTTP_FORBIDDEN) {
                throw e;
            }
            success = false;
        }
        if (!success) {
            throw new CommandFailedException("Ingreso no exitoso");
        }
        printer.info("Ingreso exitoso");
    }
}
