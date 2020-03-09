package IntelHexFormatReader.Model;

import IntelHexFormatReader.*;

/**
 * Logical representation of an Intel HEX record (a single line in an Intel HEX file).
 */
public class IntelHexRecord {
    private RecordType RecordType;

    public final RecordType getRecordType() {
        return RecordType;
    }

    public final void setRecordType(RecordType value) {
        RecordType = value;
    }

    private int ByteCount;

    public final int getByteCount() {
        return ByteCount;
    }

    public final void setByteCount(int value) {
        ByteCount = value;
    }

    private int Address;

    public final int getAddress() {
        return Address;
    }

    public final void setAddress(int value) {
        Address = value;
    }

    private byte[] Bytes;

    public final byte[] getBytes() {
        return Bytes;
    }

    public final void setBytes(byte[] value) {
        Bytes = value;
    }

    private int CheckSum;

    public final int getCheckSum() {
        return CheckSum;
    }

    public final void setCheckSum(int value) {
        CheckSum = value;
    }
}