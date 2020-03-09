import java.io.IOException;
import java.util.Arrays;

import IntelHexFormatReader.Extensions;
import IntelHexFormatReader.HexFileLineParser;
import IntelHexFormatReader.Model.IntelHexRecord;
import IntelHexFormatReader.Model.MemoryCell;

public class ExtensionsTest {

    public static void main(String[] args) throws IOException {
        IntelHexRecord record = HexFileLineParser.ParseLine(":10010000214601360121470136007EFE09D2190140");

        System.out.println(record.getByteCount());
        System.out.println(record.getAddress());
        System.out.println(record.getRecordType());
        System.out.println(Arrays.toString(record.getBytes()));
        System.out.println(record.getCheckSum());
        Extensions.Assert(record, rec -> true == true, "Impossible!");
        System.out.println("-------------------------------------------------------");
        MemoryCell cell1 = new MemoryCell(0x00);
        cell1.setValue((byte) 0xF0);
        MemoryCell cell2 = new MemoryCell(0x01);
        cell2.setValue((byte)0xB0);
        MemoryCell cell3 = new MemoryCell(0x02);
        cell3.setValue((byte)0xF0);
        MemoryCell cell4 = new MemoryCell(0x03);
        cell4.setValue((byte)0xB0);
        MemoryCell cell5 = new MemoryCell(0x04);
        cell5.setValue((byte)0xF0);
        MemoryCell cell6 = new MemoryCell(0x05);
        cell6.setValue((byte)0xF0);
        MemoryCell cell7 = new MemoryCell(0x06);
        cell7.setValue((byte)0xAA);
        MemoryCell cell8 = new MemoryCell(0x07);
        cell8.setValue((byte)0xFE);
        MemoryCell[] cells = new MemoryCell[]{cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8};

        for (int i = 0; i < cells.length; i++) {
            if (i > 0)
                System.out.print("-");
            System.out.print(String.format("%02X", cells[i].getValue()));
        }
        System.out.println();

        int test1 = Extensions.LastIndexOf(cells, cell -> cell.getValue() == (byte)0xB0);// .Should().Be(3);
        System.out.println(test1);
        int test2 = Extensions.LastIndexOf(cells, cell -> cell.getValue() == (byte)0xAA);// .Should().Be(6);
        System.out.println(test2);
        int test3 = Extensions.LastIndexOf(cells, cell -> cell.getValue() == (byte)0xF0);// .Should().Be(5);
        System.out.println(test3);
        int test4 = Extensions.LastIndexOf(cells, cell -> cell.getValue() == (byte)0xFE);// .Should().Be(7);
        System.out.println(test4);
        int test5 = Extensions.LastIndexOf(cells, cell -> cell.getValue() == (byte)0xFF);// .Should().Be(-1); // Not found
        System.out.println(test5);

    }
}
