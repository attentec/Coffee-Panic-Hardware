package com.attentec.coffeepanic;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class App
{
    private static final float ALERT_AT_GRAMS = 100;
    private static final float STABLE_THRESHOLD = 5;

    public static void main( String[] args ) {
        Logger logger = LogManager.getLogger();

        while (true) {
            try {
                connectAndRun();
            } catch (Exception e) {
                logger.error("Exception in main loop:", e);
                sleep(5);
            }
        }
    }

    private static void connectAndRun() throws ScaleException {
        ScaleLocator locator = new UsbScaleLocator();
        float lastGrams = 0;

        try (Scale scale = locator.findFirst()){
            while (true) {
                lastGrams = measure(scale, lastGrams);
                sleep(2);
            }
        }
    }

    private static float measure(Scale scale, float lastGrams) throws ScaleException {
        Measurement measurement = scale.measure();

        if (measurement.isStable()) {
            float grams = measurement.getGrams();
            boolean isStable = Math.abs(grams - lastGrams) < STABLE_THRESHOLD;

            if (isStable && grams <= ALERT_AT_GRAMS) {
                System.out.println("Low on coffee!");
            }

            lastGrams = grams;
            return grams;
        }

        return lastGrams;
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}
