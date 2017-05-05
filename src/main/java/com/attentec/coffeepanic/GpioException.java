package com.attentec.coffeepanic;

public final class GpioException extends Exception {
    public GpioException(String message) {
        super(message);
    }

    public GpioException(String message, Throwable cause) {
        super(message, cause);
    }
}
