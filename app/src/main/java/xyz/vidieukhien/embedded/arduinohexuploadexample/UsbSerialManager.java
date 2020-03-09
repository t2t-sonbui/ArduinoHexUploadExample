package xyz.vidieukhien.embedded.arduinohexuploadexample;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;


public class UsbSerialManager {
    private static final String TAG = UsbSerialManager.class.getSimpleName();
    public static final String ACTION_USB_NOT_SUPPORTED = "xyz.vidieukhien.embedded.device.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "xyz.vidieukhien.embedded.device.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "xyz.vidieukhien.embedded.device.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "xyz.vidieukhien.embedded.device.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_PERMISSION_REQUEST = "xyz.vidieukhien.embedded.device.ACTION_USB_PERMISSION_REQUEST";
    public static final String ACTION_USB_DISCONNECTED = "xyz.vidieukhien.embedded.device.ACTION_USB_DISCONNECTED";
    public static final String ACTION_USB_CONNECT = "xyz.vidieukhien.embedded.device.ACTION_USB_CONNECT";
    public static final String ACTION_USB_READY = "xyz.vidieukhien.embedded.device.USB_READY";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "xyz.vidieukhien.embedded.device.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "xyz.vidieukhien.embedded.device.ACTION_USB_DEVICE_NOT_WORKING";

    private static final int BAUD_RATE = 115200; // BaudRate. Change this value if you need
    private final Context context;
    private final UsbManager usbManager;
    //    private UsbDeviceConnection connection;
    private UsbDevice deviceWithoutPermission;
    private UsbSerialDevice device; // lazy loaded
    private UsbSerialInterface.UsbReadCallback mCallback;
    /*
     * State changes in the CTS line will be received here
     */
    private UsbSerialInterface.UsbCTSCallback ctsCallback;
    /*
     * State changes in the DSR line will be received here
     */
    private UsbSerialInterface.UsbDSRCallback dsrCallback;

    private HashMap<String, UsbDevice> usbDeviceList;

    public HashMap<String, UsbDevice> getUsbDeviceList() throws UsbSerialException {
        this.usbDeviceList = usbManager.getDeviceList();
        if ((usbDeviceList == null) || (usbDeviceList.size() == 0))
            throw new UsbSerialException(UsbSerialException.TYPE_NOT_CONNECTED);
        return usbDeviceList;
    }

    private Intent requestIntent = new Intent(ACTION_USB_PERMISSION_REQUEST);

    public UsbSerialManager(Context context) {
        this.context = context.getApplicationContext();
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

    }

    public void requestDevicePermissionForUsbDevice(String usbKey, UsbDevice usbDevice) {
        synchronized (UsbSerialManager.class) {
            usbManager.requestPermission(usbDevice, PendingIntent.getBroadcast(context, 0, requestIntent, 0));
        }
    }

    //
    // PRIVATE
    //
    boolean deviceIsUsbSerial(UsbDevice usbDevice) {
        synchronized (UsbSerialManager.class) {
            // no usable interfaces
            if (usbDevice.getInterfaceCount() <= 0) {
                return false;
            }
            int deviceVID = usbDevice.getVendorId();
            int devicePID = usbDevice.getProductId();

            if (deviceVID != 0x1d6b) {//Danh sach thiet bi
                // There is a device connected to our Android device. Try to open it as a Serial Port.
                return true;
            }
            return false;
        }
    }

    public void getDevicePermission(String usbKey) {
        synchronized (UsbSerialManager.class) {
            this.usbDeviceList = usbManager.getDeviceList();
            UsbDevice usbDevice = usbDeviceList.get(usbKey);
            if (!usbManager.hasPermission(usbDevice)) {
                requestDevicePermissionForUsbDevice(usbKey, usbDevice);
            }
        }
    }

    public boolean checkDevicePermission(String usbKey) {
        synchronized (UsbSerialManager.class) {
            this.usbDeviceList = usbManager.getDeviceList();
            UsbDevice usbDevice = usbDeviceList.get(usbKey);
            return usbManager.hasPermission(usbDevice);
        }
    }

    public UsbSerialDevice tryGetDevice(String key) {
        synchronized (UsbSerialManager.class) {
            if (device == null) {
                Log.i(TAG, "tryGetDevice: trying to find and open connection to usb serial");
                this.usbDeviceList = usbManager.getDeviceList();
                UsbDevice usbDevice = usbDeviceList.get(key);
                if (!deviceIsUsbSerial(usbDevice)) {
                    Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                    context.sendBroadcast(intent);
                    return null;
                }
                Log.i(TAG, "tryGetDevice: Serial device found");
                if (!usbManager.hasPermission(usbDevice)) {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    context.sendBroadcast(intent);
                    return null;
                }
                UsbDeviceConnection conn = usbManager.openDevice(usbDevice);
                if (conn == null) {
                    Log.e(TAG, "tryGetDevice: could not open connection");
                } else {
                    device = UsbSerialDevice.createUsbSerialDevice(usbDevice, conn);
                    if (device != null) {
                        if (device.open()) {
                            device.setBaudRate(BAUD_RATE);
                            device.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            device.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            device.setParity(UsbSerialInterface.PARITY_NONE);
                            device.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            if (mCallback != null) device.read(mCallback);
                            if (ctsCallback != null) device.getCTS(ctsCallback);
                            if (dsrCallback != null) device.getDSR(dsrCallback);
                            Intent intent = new Intent(ACTION_USB_READY);
                            context.sendBroadcast(intent);
                            Log.i(TAG, "tryGetDevice: ACTION_USB_READY ");
                            device.close();//close before work
                        } else {
                            // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                            // Send an Intent to Main Activity
                            if (device instanceof CDCSerialDevice) {
                                Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                                context.sendBroadcast(intent);
                                Log.i(TAG, "tryGetDevice: ACTION_CDC_DRIVER_NOT_WORKING ");
                            } else {
                                Log.i(TAG, "tryGetDevice: ACTION_USB_DEVICE_NOT_WORKING ");
                                Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                                context.sendBroadcast(intent);
                            }
                        }

                    } else {
                        // No driver for given device, even generic CDC driver could not be loaded
                        Log.i(TAG, "tryGetDevice: ACTION_USB_NOT_SUPPORTED ");
                        Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                        context.sendBroadcast(intent);
                        return null;
                    }
                }
                Log.i(TAG, "tryGetDevice: " + (this.device == null ? "Usb device not found or failed to connect" : "Usb device successfully connected"));
            } else {
                Log.i(TAG, "tryGetDevice: using already connected device");
            }
            return device;
        }
    }
}
