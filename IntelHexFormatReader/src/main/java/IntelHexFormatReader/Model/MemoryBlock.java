package IntelHexFormatReader.Model;

import IntelHexFormatReader.*;

/**
 * Logical representation of a MemoryBlock (an ordered collection of memory cells and registers).
 */
public class MemoryBlock {
    // CS & IP registers for 80x86 systems.
    /**
     * Code Segment register (16-bit).
     */
    private short CS;

    public final short getCS() {
        return CS;
    }

    public final void setCS(short value) {
        CS = value;
    }

    /**
     * Instruction Pointer register (16-bit).
     */
    private short IP;

    public final short getIP() {
        return IP;
    }

    public final void setIP(short value) {
        IP = value;
    }

    // EIP register for 80386 and higher CPU's.

    /**
     * Extended Instruction Pointer register (32-bit).
     */
    private int EIP;

    public final int getEIP() {
        return EIP;
    }

    public final void setEIP(int value) {
        EIP = value;
    }

    /**
     * Returns the index of the highest modified cell.
     */
    public final int getHighestModifiedOffset() {
        return IntelHexFormatReader.Extensions.LastIndexOf(getCells(), MemoryCell::getModified);
    }

    /**
     * Returns the size of this memory, in bytes.
     */
    public final int getMemorySize() {
        return getCells().length;
    }

    /**
     * Memory cells in this memory block.
     */
    private MemoryCell[] Cells;

    public final MemoryCell[] getCells() {
        return Cells;
    }

    public final void setCells(MemoryCell[] value) {
        Cells = value;
    }

    /**
     * Construct a new MemoryBlock.
     *
     * @param memorySize The size of the MemoryBlock to instantiate.
     */

    public MemoryBlock(int memorySize) {
        this(memorySize, (byte) 0xff);
    }
    
    public MemoryBlock(int memorySize, byte fillValue) {
        setCells(new MemoryCell[memorySize]);
        for (int i = 0; i < memorySize; i++) {
            MemoryCell tempVar = new MemoryCell(i);
            tempVar.setValue(fillValue);
            getCells()[i] = tempVar;
        }
    }
}