package com.attentec.coffeepanic;

final class Message {
    private static int DATA_MODE_GRAMS = 2;
    private static int DATA_MODE_OUNCES = 11;

    private static float OUNCES_TO_GRAMS = 28.3495231f;

    private final byte data[];

    Message(byte[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("data size must be at least 6");
        }

        this.data = data;
    }

    public boolean isStable() {
        return data[1] > 1;
    }

    public float getGrams() {
        float rawWeight = data[4] + (256.0f * (float)data[5]);
        byte mode = data[2];

        if (mode == DATA_MODE_OUNCES) {
            float scaleFactor = (float)Math.pow(10.0f, (float)data[3]);
            float ounces = rawWeight * scaleFactor;
            return OUNCES_TO_GRAMS * ounces;
        } else if (mode == DATA_MODE_GRAMS) {
            return rawWeight;
        }

        throw new IllegalArgumentException("invalid mode");
    }

    public String toString() {
        return String.format("%d%d%d%d%d%d", data[0], data[1], data[2], data[3], data[4], data[5]);
    }
}
