package com.console.atm.cli;

/** Una operacion del cajero invocable por linea de comando. */
public interface Command {

    /** Nombre con el que se invoca, por ejemplo "login". */
    String getName();

    /** Forma de uso sin el nombre de la aplicacion, por ejemplo "login <tarjeta>". */
    String getUsage();

    /** Cantidad de argumentos que recibe, sin contar el nombre del comando. */
    int getArgCount();

    /**
     * Ejecuta la operacion. Los errores del servicio se propagan como AtmApiException o
     * AtmUnavailableException y un importe invalido como IllegalArgumentException.
     *
     * @param args argumentos del comando, sin el nombre
     */
    void execute(String[] args);
}
