package com.console.atm.cli;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ConsolePrinterTest {

    @Test
    public void usesConsoleEncodingWhenSupported() {
        assertEquals(Charset.forName("IBM850"), ConsolePrinter.resolveCharset("cp850"));
    }

    @Test
    public void usesUtf8ForUtf8Console() {
        assertEquals(StandardCharsets.UTF_8, ConsolePrinter.resolveCharset("cp65001"));
    }

    @Test
    public void usesUtf8WhenOutputIsRedirected() {
        assertEquals(StandardCharsets.UTF_8, ConsolePrinter.resolveCharset(null));
    }

    @Test
    public void usesUtf8ForUnknownEncoding() {
        assertEquals(StandardCharsets.UTF_8, ConsolePrinter.resolveCharset("no-existe"));
    }
}
