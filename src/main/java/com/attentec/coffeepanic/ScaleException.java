package com.attentec.coffeepanic;

public class ScaleException extends Exception {
    public ScaleException(String message) {
        super(message);
    }

    public ScaleException(String message, Throwable cause) {
        super(message, cause);
    }
}
