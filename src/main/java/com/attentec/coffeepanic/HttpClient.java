package com.attentec.coffeepanic;

import java.io.Closeable;

public interface HttpClient extends Closeable {
    @Override
    public void close() throws HttpException;

    public void postMeasurement(float grams) throws HttpException;
}
