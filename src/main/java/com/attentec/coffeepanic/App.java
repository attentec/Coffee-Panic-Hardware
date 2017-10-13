package com.attentec.coffeepanic;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class App
{
    private static final float ALERT_AT_GRAMS = 100;
    private static final float STABLE_THRESHOLD = 5;

    public static void main(String[] args) {
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

    private static void connectAndRun() throws GpioException, ScaleException {
        GpioPin changeUnit = createOutputPin(2);
        GpioPin power = createOutputPin(3);

        ScaleLocator locator = new UsbScaleLocator();
        float lastGrams = 0;
        int iteration = 0;

        try (Scale scale = locator.findFirst()) {
            while (true) {
                lastGrams = measure(scale, lastGrams);

                if (iteration == 10) {
                    press(changeUnit);
                    iteration = 0;
                } else {
                    ++iteration;
                }

                sleep(2);
            }
        } catch (ScaleNotFound e) {
            press(power);
            throw e;
        }
    }

    private static GpioPin createOutputPin(int pin) throws GpioException {
        GpioPin gpioPin = new GpioPin(pin);
        gpioPin.setDirection(GpioPin.Direction.OUT);
        gpioPin.setValue(false);

        return gpioPin;
    }

    private static void press(GpioPin pin) throws GpioException {
        pin.setValue(true);
        sleep(0.5);
        pin.setValue(false);
    }

    private static float measure(Scale scale, float lastGrams) throws ScaleException {
        Measurement measurement = scale.measure();

        return measurement.getGrams().map((Float grams) -> {
            boolean isStable = Math.abs(grams - lastGrams) < STABLE_THRESHOLD;

            if (isStable && grams <= ALERT_AT_GRAMS) {
                System.out.println("Low on coffee!");
            }

            return grams;
        }).orElse(lastGrams);
    }

    private static void sleep(double seconds) {
        try {
            Thread.sleep((int)(1000 * seconds));
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}
