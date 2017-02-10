package com.attentec.coffeepanic;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.UnsupportedEncodingException;

/**
 * Hello world!
 *
 */
public class App 
{

    public static void main( String[] args )
    {
        UsbService sc = new UsbService();
        UsbDevice device = sc.findDevice((short) 0x0922, (short)0x8003);
        try {
            sc.readMessage(sc.getDeviceInterface(device,0), 0);
            System.out.println(device.getManufacturerString());
        } catch (UsbException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


}
