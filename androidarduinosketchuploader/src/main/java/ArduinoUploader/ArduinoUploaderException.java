package ArduinoUploader;

public class ArduinoUploaderException extends RuntimeException {
    private String message;

    public ArduinoUploaderException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}