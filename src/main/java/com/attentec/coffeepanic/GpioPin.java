package com.attentec.coffeepanic;

import java.io.IOException;
import java.io.PrintWriter;

public final class GpioPin
{
    enum Direction {
        IN,
        OUT,
    }

    private static final String ROOT = "/sys/class/gpio/";
    private final String path;

    public GpioPin(int pin) throws GpioException {
        path = ROOT + "gpio" + Integer.toString(pin) + '/';
        writeFile(ROOT + "export", Integer.toString(pin));
    }

    public void setDirection(Direction direction) throws GpioException {
        write("direction", direction == Direction.IN ? "in" : "out");
    }

    public void setValue(boolean value) throws GpioException {
        write("value", value ? "1" : "0");
    }

    private void write(String property, String value) throws GpioException {
        writeFile(path + property, value);
    }

    private static void writeFile(String filename, String contents) throws GpioException {
        // The PrinterWriter can write to /sys, while Files cannot
        try (PrintWriter writer = new PrintWriter(filename, "US-ASCII")) {
            writer.print(contents);
        } catch (IOException e) {
            throw new GpioException("Failed to write to file \"" + filename + "\"", e);
        }
    }
}
