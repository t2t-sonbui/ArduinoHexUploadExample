package xyz.vidieukhien.embedded.arduinohexuploadexample;

public class UsbSerialException extends RuntimeException {
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int TYPE_NEEDS_PERMISSION = 1;
    public static final int TYPE_COMMUNICATION_ERROR = 2;
    public static final int TYPE_READ_TIMEOUT_ERROR = 3;
    public static final int TYPE_WRITE_TIMEOUT_ERROR = 4;
    public static final int TYPE_UNEXPECTED_RESPONSE = 5;

    private final int type;

    public UsbSerialException(int type) {
        this.type = type;
    }

    public UsbSerialException(int type, Throwable throwable) {
        super(throwable);
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
