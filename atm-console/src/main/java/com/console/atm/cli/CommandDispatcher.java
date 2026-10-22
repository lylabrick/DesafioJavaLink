package com.console.atm.cli;

import com.console.atm.client.AtmApiException;
import com.console.atm.client.AtmUnavailableException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Elige el comando segun el primer argumento, valida su cantidad de argumentos y traduce errores a un exit code. */
public class CommandDispatcher {

    public static final int EXIT_OK = 0;
    public static final int EXIT_ERROR = 1;

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final ConsolePrinter printer;

    public CommandDispatcher(List<Command> commandList, ConsolePrinter printer) {
        this.printer = printer;
        for (Command command : commandList) {
            commands.put(command.getName(), command);
        }
    }

    /** @return 0 si la operacion termino bien, 1 en cualquier otro caso */
    public int dispatch(String[] args) {
        if (args.length == 0) {
            printUsage();
            return EXIT_ERROR;
        }

        Command command = commands.get(args[0].toLowerCase());
        if (command == null) {
            printer.error("Comando desconocido: " + args[0]);
            printUsage();
            return EXIT_ERROR;
        }

        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        if (commandArgs.length != command.getArgCount()) {
            printer.error("Uso: atm " + command.getUsage());
            return EXIT_ERROR;
        }

        try {
            command.execute(commandArgs);
            return EXIT_OK;
        } catch (CommandFailedException | AtmApiException | IllegalArgumentException e) {
            printer.error(e.getMessage());
        } catch (AtmUnavailableException e) {
            printer.error("Error: " + e.getMessage() + ". Verifique que el servicio este en ejecucion.");
        }
        return EXIT_ERROR;
    }

    private void printUsage() {
        printer.error("Uso: atm <comando> [argumentos]");
        printer.error("Comandos disponibles:");
        for (Command command : commands.values()) {
            printer.error("  atm " + command.getUsage());
        }
    }
}
