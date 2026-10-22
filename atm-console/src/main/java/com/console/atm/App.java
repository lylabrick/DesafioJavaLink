package com.console.atm;

import com.console.atm.cli.impl.BalanceCommand;
import com.console.atm.cli.Command;
import com.console.atm.cli.CommandDispatcher;
import com.console.atm.cli.ConsolePrinter;
import com.console.atm.cli.impl.DepositCommand;
import com.console.atm.cli.impl.ExtractCommand;
import com.console.atm.cli.impl.LoginCommand;
import com.console.atm.client.AtmClient;
import com.console.atm.client.HttpAtmClient;
import com.console.atm.config.AtmConfig;

import java.util.Arrays;
import java.util.List;

/** Punto de entrada de la aplicacion de consola del cajero automatico. */
public class App {

    public static void main(String[] args) {
        ConsolePrinter printer = ConsolePrinter.forSystemStreams();
        AtmClient client = new HttpAtmClient(AtmConfig.defaults());

        List<Command> commands = Arrays.asList(
                new LoginCommand(client, printer),
                new ExtractCommand(client, printer),
                new DepositCommand(client, printer),
                new BalanceCommand(client, printer));

        System.exit(new CommandDispatcher(commands, printer).dispatch(args));
    }
}
