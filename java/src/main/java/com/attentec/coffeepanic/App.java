package com.attentec.coffeepanic;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.UnsupportedEncodingException;

public class App
{
    private static float ALERT_AT_GRAMS = 100;
    private static float STABLE_THRESHOLD = 5;

    public static void main( String[] args )
    {
        UsbService sc = new UsbService();
        UsbDevice device = sc.findDevice((short) 0x0922, (short)0x8003);

        float lastGrams = 0.0f;
        Message message = sc.readMessage(sc.getDeviceInterface(device,0), 0);

        if (message.isStable()) {
            float grams = message.getGrams();
            boolean isStable = Math.abs(grams - lastGrams) < STABLE_THRESHOLD;

            if (isStable && grams <= ALERT_AT_GRAMS) {
                System.out.println("Low on coffee!");
            }

            lastGrams = grams;
        }
    }
}
