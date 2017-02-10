package com.attentec.coffeepanic;

import java.util.List;
import javax.usb.*;

final class UsbScale implements Scale {
    private static int DATA_MODE_GRAMS = 2;
    private static int DATA_MODE_OUNCES = 11;

    private static float OUNCES_TO_GRAMS = 28.3495231f;

    private UsbInterface usbInterface;
    private UsbPipe pipe;

    public UsbScale(UsbInterface usbInterface, UsbPipe pipe) {
        this.usbInterface = usbInterface;
        this.pipe = pipe;
    }

    public Measurement measure() throws ScaleException {
        byte[] data = new byte[8];

        try {
            int received = pipe.syncSubmit(data);

            if (received < 6) {
                throw new ScaleException("Could not read enough data from the scale");
            }

            final boolean isStable = data[1] > 1;
            final float grams = getGrams(data);

            return new Measurement() {
                @Override
                public boolean isStable() {
                    return isStable;
                }

                @Override
                public float getGrams() {
                    return grams;
                }
            };
        } catch (UsbException e) {
            throw new ScaleException("Failed to measure", e);
        }
    }

    private static float getGrams(byte[] data) throws ScaleException {
        byte mode = data[2];
        float rawWeight = data[4] + (256.0f * (float)data[5]);

        if (mode == DATA_MODE_OUNCES) {
            float scaleFactor = (float)Math.pow(10.0f, (float)data[3]);
            float ounces = rawWeight * scaleFactor;
            return OUNCES_TO_GRAMS * ounces;
        } else if (mode == DATA_MODE_GRAMS) {
            return rawWeight;
        }

        throw new ScaleException("Invalid mode returned from scale");
    }

    @Override
    public void close() {
        if (pipe != null) {
            try {
                pipe.close();
            } catch (UsbException e) {
                // ignore
            }

            pipe = null;
        }

        if (usbInterface != null) {
            try {
                usbInterface.release();
            } catch (UsbException e) {
                // ignore
            }

            usbInterface = null;
        }
    }
}

public final class UsbScaleLocator implements ScaleLocator {
    private static final short VENDOR_ID = (short) 0x0922;
    private static final short PRODUCT_ID = (short) 0x8003;

    public Scale findFirst() throws ScaleException {
        try {
            UsbServices services = UsbHostManager.getUsbServices();
            UsbHub rootHub = services.getRootUsbHub();
            UsbDevice scale = findFirstScaleDevice(rootHub);

            if (scale == null) {
                throw new ScaleNotFound();
            }

            UsbConfiguration configuration = scale.getActiveUsbConfiguration();
            UsbInterface usbInterface = configuration.getUsbInterface((byte) 0);

            usbInterface.claim(new UsbInterfacePolicy() {
                public boolean forceClaim(UsbInterface usbInterface) {
                    return true;
                }
            });

            try {
                List endpoints = usbInterface.getUsbEndpoints();

                if (endpoints.size() == 0) {
                    throw new ScaleException("Failed to get USB endpoint");
                }

                UsbEndpoint endpoint = (UsbEndpoint)endpoints.get(0);
                UsbPipe pipe = endpoint.getUsbPipe();
                pipe.open();

                return new UsbScale(usbInterface, pipe);
            } catch (Exception e) {
                usbInterface.release();
                throw e;
            }
        } catch (UsbException e) {
            throw new ScaleException("Failed to open device", e);
        }
    }

    private UsbDevice findFirstScaleDevice(UsbHub hub) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor();
            UsbDevice scale = null;

            if (descriptor.idVendor() == VENDOR_ID && descriptor.idProduct() == PRODUCT_ID) {
                scale = device;
            }

            if (scale == null && device.isUsbHub()) {
                scale = findFirstScaleDevice((UsbHub) device);
            }

            if (scale != null) {
                return scale;
            }
        }

        return null;
    }
}
