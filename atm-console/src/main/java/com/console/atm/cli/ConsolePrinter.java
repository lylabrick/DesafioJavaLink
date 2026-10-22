package com.console.atm.cli;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Unico punto de salida por consola: resultados a stdout y errores a stderr. */
public class ConsolePrinter {

    private final PrintStream out;
    private final PrintStream err;

    public ConsolePrinter(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    /** Escribe en stdout/stderr con una codificacion que la consola pueda mostrar (por ejemplo, "Depósito"). */
    public static ConsolePrinter forSystemStreams() {
        return new ConsolePrinter(
                stream(FileDescriptor.out, System.getProperty("sun.stdout.encoding")),
                stream(FileDescriptor.err, System.getProperty("sun.stderr.encoding")));
    }

    /**
     * Elige la codificacion de salida a partir de la que informa la JVM para la consola.
     * Java 8 no reconoce cp65001 (consola en UTF-8) y, si la salida esta redirigida, no informa
     * ninguna; en esos casos usar la codificacion por defecto (Cp1252) rompe los acentos, por eso se usa UTF-8.
     */
    static Charset resolveCharset(String consoleEncoding) {
        if (consoleEncoding == null || "cp65001".equalsIgnoreCase(consoleEncoding)) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(consoleEncoding);
        } catch (IllegalArgumentException e) {
            return StandardCharsets.UTF_8;
        }
    }

    private static PrintStream stream(FileDescriptor fd, String consoleEncoding) {
        try {
            return new PrintStream(new FileOutputStream(fd), true, resolveCharset(consoleEncoding).name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void info(String message) {
        out.println(message);
    }

    public void error(String message) {
        err.println(message);
    }
}
