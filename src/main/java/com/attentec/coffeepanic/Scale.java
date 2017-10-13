package com.attentec.coffeepanic;

import java.io.Closeable;

public interface Scale extends Closeable {
    public Measurement measure() throws ScaleException;

    public void close();
}
