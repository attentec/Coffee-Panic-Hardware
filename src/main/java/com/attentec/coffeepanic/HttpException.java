package com.attentec.coffeepanic;

import java.io.IOException;

public class HttpException extends IOException {
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
