package IntelHexFormatReader.Model;

import IntelHexFormatReader.*;

public enum RecordType {
    Data,
    EndOfFile,
    ExtendedSegmentAddress,
    StartSegmentAddress,
    ExtendedLinearAddress,
    StartLinearAddress;
    public static final int SIZE = java.lang.Integer.SIZE;

    public int getValue() {
        return this.ordinal();
    }

    public static RecordType forValue(int value) {
        return values()[value];
    }

    public static boolean IsDefined(int legIndex) {
        for (RecordType l : RecordType.values()) {
            if (l.getValue() == legIndex)
                return true;
        }
        return false;
    }
}