package com.attentec.coffeepanic;

import java.io.Closeable;

public interface Scale extends Closeable {
    Measurement measure() throws ScaleException;

    void close();
}
