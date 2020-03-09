package IntelHexFormatReader;

import IntelHexFormatReader.Model.*;
import IntelHexFormatReader.Utils.FileLineIterable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

public class HexFileReader {
    private Iterable<String> hexRecordLines;
    private int memorySize;

    public HexFileReader(String fileName, int memorySize) {
        try {
            Iterable<String> lines = new FileLineIterable(fileName);
            Initialize(lines, memorySize);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(String.format("File %1$s does not exist!", fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HexFileReader(Iterable<String> hexFileContents, int memorySize) {
        Initialize(hexFileContents, memorySize);
    }

    private void Initialize(Iterable<String> lines, int memSize) {

        if (!lines.iterator().hasNext()) {
            throw new IllegalArgumentException("Hex file contents can not be empty!");
        }
        if (memSize <= 0) {
            throw new IllegalArgumentException("Memory size must be greater than zero!");
        }
        hexRecordLines = lines;
        memorySize = memSize;
    }

    /**
     * Parse the currently loaded HEX file contents.
     *
     * @return A MemoryBlock representation of the HEX file.
     */
    public final MemoryBlock Parse() throws IOException {
        return ReadHexFile(hexRecordLines, memorySize);
    }

    private static MemoryBlock ReadHexFile(Iterable<String> hexRecordLines, int memorySize) throws IOException {
        MemoryBlock result = new MemoryBlock(memorySize);
        int baseAddress = 0;
        boolean encounteredEndOfFile = false;
        for (String hexRecordLine : hexRecordLines) {
            IntelHexFormatReader.Model.IntelHexRecord hexRecord = HexFileLineParser.ParseLine(hexRecordLine);
            switch (hexRecord.getRecordType()) {
                case Data: {
                    int nextAddress = hexRecord.getAddress() + baseAddress;
                    for (int i = 0; i < hexRecord.getByteCount(); i++) {
                        if (nextAddress + i > memorySize) {
                            throw new IOException(String.format("Trying to write to position %1$s outside of memory boundaries (%2$s)!", nextAddress + i, memorySize));
                        }

                        IntelHexFormatReader.Model.MemoryCell cell = result.getCells()[nextAddress + i];
                        cell.setValue(hexRecord.getBytes()[i]);
                        cell.setModified(true);
                    }
                    break;
                }
                case EndOfFile: {
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getAddress() == 0, "Address should equal zero in EOF.");
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getByteCount() == 0, "Byte count should be zero in EOF.");
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getBytes().length == 0, "Number of bytes should be zero for EOF.");
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getCheckSum() == 0xff, "Checksum should be 0xff for EOF.");
                    encounteredEndOfFile = true;
                    break;
                }
                case ExtendedSegmentAddress: {
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getByteCount() == 2, "Byte count should be 2.");
                    baseAddress = (hexRecord.getBytes()[0] << 8 | hexRecord.getBytes()[1]) << 4;
                    break;
                }
                case ExtendedLinearAddress: {
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getByteCount() == 2, "Byte count should be 2.");
                    baseAddress = (hexRecord.getBytes()[0] << 8 | hexRecord.getBytes()[1]) << 16;
                    break;
                }
                case StartSegmentAddress: {
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getByteCount() == 4, "Byte count should be 4.");
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getAddress() == 0, "Address should be zero.");
                    result.setCS((short) (hexRecord.getBytes()[0] << 8 + hexRecord.getBytes()[1]));
                    result.setIP((short) (hexRecord.getBytes()[2] << 8 + hexRecord.getBytes()[3]));
                    break;
                }
                case StartLinearAddress:
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getByteCount() == 4, "Byte count should be 4.");
                    IntelHexFormatReader.Extensions.Assert(hexRecord, rec -> rec.getAddress() == 0, "Address should be zero.");
                    result.setEIP((int) (hexRecord.getBytes()[0] << 24) + (int) (hexRecord.getBytes()[1] << 16) + (int) (hexRecord.getBytes()[2] << 8) + hexRecord.getBytes()[3]);
                    break;
            }
        }
        if (!encounteredEndOfFile) {
            throw new IOException("No EndOfFile marker found!");
        }
        return result;
    }
}