#!/usr/bin/env python
import usb.core
import usb.util
import ctypes
from time import sleep

VENDOR_ID = 0x0922
PRODUCT_ID = 0x8003
DATA_MODE_GRAMS = 2
DATA_MODE_OUNCES = 11

reattach = False

# find the USB device
device = usb.core.find(idVendor=VENDOR_ID,
                       idProduct=PRODUCT_ID)
if not device:
    print "Turn device on stupid."
    raise Exception("User error")

if device.is_kernel_driver_active(0):
    reattach = True
    device.detach_kernel_driver(0)


    # use the first/default configuration
device.set_configuration()
# first endpoint
endpoint = device[0][(0,0)][0]

# read a data packet
try:
    grams_before = 0.0
    while 1:
        sleep(1)
        attempts = 10
        data = None
        while data is None and attempts > 0:
            try:
                data = device.read(endpoint.bEndpointAddress,
                                   endpoint.wMaxPacketSize)
            except usb.core.USBError as e:
                data = None
                if e.args == ('Operation timed out',):
                    attempts -= 1
                    continue
        if attempts == 0:
            print "Failed to read value from scale 10 times!"
        if data[1] < 2:
            print "Data value unstable!"
            continue
        raw_weight = data[4] + data[5] * 256
        scale_factor = float(ctypes.c_byte(data[3]).value)
        scale_factor = pow(10.0, scale_factor)
        if data[2] == DATA_MODE_OUNCES:
            grams = float(raw_weight) * scale_factor * 28.3495231
        elif data[2] == DATA_MODE_GRAMS:
            grams = raw_weight
        # check that less than 2% changed since last measurement
        if abs(grams-grams_before) > 5:
            #print "Value unstable!"
            grams_before = grams
            continue
        grams_before = grams
        print "Coffee: %.2f grams" % grams
        if grams < 10:
            print "ALERT!"
except KeyboardInterrupt:
    pass
finally:
    if reattach:
        device.attach_kernel_driver(0)
