package IntelHexFormatReader.Model;

import IntelHexFormatReader.*;

/**
 * Logical representation of a MemoryCell.
 */
public class MemoryCell {
    private int Address;

    public final int getAddress() {
        return Address;
    }

    private void setAddress(int value) {
        Address = value;
    }

    private boolean Modified;

    public final boolean getModified() {
        return Modified;
    }

    public final void setModified(boolean value) {
        Modified = value;
    }

    private byte Value;

//    public final int getValue() {
//        return (int) Value & 0xFF; //This fix https://stackoverflow.com/questions/38392565/compare-a-byte-and-hexadecimal
//    }
    public final byte getValue() {
        return  Value ;
    }

    public final void setValue(byte value) {
        Value = value;
    }

    public MemoryCell(int address) {
        setAddress(address);
    }

    @Override
    public String toString() {
        return String.format("MemoryCell : %1$s Value: %2$s (modified = %3$s)", String.format("%08X", getAddress()), String.format("%02X", getValue()), getModified());
    }
}