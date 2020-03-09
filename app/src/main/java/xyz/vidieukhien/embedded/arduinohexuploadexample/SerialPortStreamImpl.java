package xyz.vidieukhien.embedded.arduinohexuploadexample;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import ArduinoUploader.Help.ISerialPortStream;



public class SerialPortStreamImpl implements ISerialPortStream {
    private static final String TAG = SerialPortStreamImpl.class.getSimpleName();
    protected int baudRate;
    protected UsbSerialDevice serialPort;
    private int readTimeout = 0;
    private int writeTimeout = 0;
    private Context mContext;
    private UsbManager usbManager;
    private UsbDeviceConnection connection;
    protected String portName;//PortKey
    HashMap<String, UsbDevice> usbDevices;
     final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;
    protected ArrayBlockingQueue<Integer> data = new ArrayBlockingQueue<Integer>(DEFAULT_READ_BUFFER_SIZE);
    protected volatile boolean is_open;

    private UsbSerialInterface.UsbReadCallback mReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] new_data) {
            for (byte b : new_data) {
                try {
                    data.put(((int) b) & 0xff);//Khong put truc tiep byte vi co gia tri -1 neu ko nhan duoc
                } catch (InterruptedException e) {
                    // ignore, possibly losing bytes when buffer is full
                }
            }
        }
    };

    public SerialPortStreamImpl(Context context, String portKey, int baud) {
        this.baudRate = baud;
        this.mContext = context;
        this.portName = portKey;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.usbDevices = usbManager.getDeviceList();
        UsbDevice usbDevice = usbDevices.get(this.portName);
        connection = usbManager.openDevice(usbDevice);
        serialPort = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection);
        //
        serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
        serialPort.setParity(UsbSerialInterface.PARITY_NONE);
    }

    @Override
    public String getPortName() {
        return this.portName;
    }

    @Override
    public String[] getPortNames() {
        List<String> portNames = new ArrayList<>();
        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            UsbDevice usbDevice = entry.getValue();
            int deviceVID = usbDevice.getVendorId();
            int devicePID = usbDevice.getProductId();
            String deviceName = usbDevice.getDeviceName();
            String deviceKey = entry.getKey();
            if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
                // There is a device connected to our Android device. Try to open it as a Serial Port.
                portNames.add(deviceKey);
                System.out.println("deviceKey:" + deviceKey + "-> " + usbDevice.toString());
            }
        }
        return portNames.toArray(new String[portNames.size()]);
    }

    @Override
    public Context getContext() {
        return this.mContext;
    }

    @Override
    public void setContext(Context context) {
        this.mContext = context;
    }

    @Override
    public void setBaudRate​(int newBaudRate) {
        synchronized (this) {
            serialPort.setBaudRate(newBaudRate);
        }

    }

    @Override
    public void setReadTimeout(int miliseconds) {
        synchronized (this) {
            readTimeout = miliseconds;
        }
    }

    @Override
    public void setWriteTimeout(int miliseconds) {
        synchronized (this) {
            writeTimeout = miliseconds;
        }
    }

    @Override
    public void open() {
        synchronized (this) {
            serialPort.open();
            serialPort.setBaudRate(this.baudRate);
            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            is_open = true;
            data.clear();
            serialPort.read(mReadCallback);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            serialPort.close();
            is_open = false;

        }
    }

    @Override
    public void setDtrEnable(boolean enable) {
        synchronized (this) {
            serialPort.setDTR(enable);
        }
    }

    @Override
    public void setRtsEnable(boolean enable) {
        synchronized (this) {
            serialPort.setRTS(enable);
        }
    }

    public void setNumDataBits​(int newDataBits) {
        synchronized (this) {
            serialPort.setDataBits(newDataBits);
        }
    }

    @Override
    public void setNumStopBits​(int newStopBits) {
        synchronized (this) {
            serialPort.setStopBits(newStopBits);
        }
    }

    ;

    @Override
    public void setParity​(int newParity) {
        synchronized (this) {
            serialPort.setParity(newParity);
        }
    }

    @Override
    public int readBytes​(byte[] buffer, int bytesToRead) {
        synchronized (this) {
            int index = 0;
            int count = 0;
            while (is_open && index < bytesToRead) {
                try {
                    int readByte = data.poll(readTimeout, TimeUnit.MILLISECONDS);
                    if (readByte != -1) {
                        buffer[index] = (byte) readByte;
                        count++;
                        index++;
                    } else return -1;
                } catch (InterruptedException e) {
                    // ignore, will be retried by while loop
                } catch (NullPointerException e) {
                    return -1;
                }
            }
            return count;
        }
    }

    @Override
    public int readBytes​(byte[] buffer, int bytesToRead, int offset) {
        synchronized (this) {
            int index = offset;
            int numRead = 0;
            while (is_open && index < bytesToRead) {
                try {
                    int readByte = data.poll(readTimeout, TimeUnit.MILLISECONDS);
                    if (readByte != -1) {

                        buffer[index] = (byte) readByte;
                        index++;
                        numRead++;
                    } else return -1;
                } catch (InterruptedException e) {
                    // ignore, will be retried by while loop
                } catch (NullPointerException e) {
                    return -1;
                }
            }
            return numRead;
        }
    }

    @Override
    public int writeBytes​(byte[] buffer, int bytesToWrite) {
        synchronized (this) {
            byte[] mWriteBuffer = new byte[bytesToWrite];
            System.arraycopy(buffer, 0, mWriteBuffer, 0, bytesToWrite);
            serialPort.write(mWriteBuffer);
            return bytesToWrite;
        }
    }

    @Override
    public int writeBytes​(byte[] buffer, int bytesToWrite, int offset) {
        synchronized (this) {
            byte[] mWriteBuffer = new byte[bytesToWrite];
            System.arraycopy(buffer, offset, mWriteBuffer, 0, bytesToWrite);
            serialPort.write(mWriteBuffer);
            return bytesToWrite;
        }
    }

    @Override
    public void DiscardInBuffer() {
        data.clear();
    }


}
