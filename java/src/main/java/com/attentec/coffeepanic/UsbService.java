package com.attentec.coffeepanic;

import javax.usb.UsbClaimException;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;
import java.util.List;

public class UsbService {
    public UsbInterface getDeviceInterface(UsbDevice device, int index) {

        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        UsbInterface iface = (UsbInterface) configuration.getUsbInterfaces().get(index); // there can be more 1,2,3..

        return iface;
    }
    public void readMessage(UsbInterface iface,
                            int endPoint){

        UsbPipe pipe = null;

        try {
            iface.claim(new UsbInterfacePolicy() {
                public boolean forceClaim(UsbInterface usbInterface) {
                    return true;
                }
            });

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();
            pipe.open();

            byte[] data = new byte[8];
            int received = pipe.syncSubmit(data);
            System.out.println(received + " bytes received");

            pipe.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                iface.release();
            } catch (UsbClaimException e) {
                e.printStackTrace();
            } catch (UsbNotActiveException e) {
                e.printStackTrace();
            } catch (UsbDisconnectedException e) {
                e.printStackTrace();
            } catch (UsbException e) {
                e.printStackTrace();
            }
        }

    }
    public UsbDevice getUsbRootHoob() {

    try {
        final UsbServices services = UsbHostManager.getUsbServices();
        return services.getRootUsbHub();
    } catch (SecurityException e) {
        e.printStackTrace();
    } catch (UsbException e) {
        e.printStackTrace();
    }

    return null;
}


    public UsbDevice findDevice(short vendorId, short productId) {

        return findDevice((UsbHub) getUsbRootHoob(), vendorId, productId);
    }

    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }
}
