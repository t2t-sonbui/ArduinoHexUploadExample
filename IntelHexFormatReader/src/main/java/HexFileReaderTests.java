import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import IntelHexFormatReader.HexFileReader;
import IntelHexFormatReader.Model.MemoryBlock;
import IntelHexFormatReader.Model.MemoryCell;

public class HexFileReaderTests {

	static String getRandomString() {
		int r = (int) (Math.random() * 4);
		String name = new String[] { ":10010000214601360121470136007EFE09D2190140",
				":100110002146017E17C20001FF5F16002148011928", ":10012000194E79234623965778239EDA3F01B2CAA7",
				":100130003F0156702B5E712B722B732146013421C7" }[r];
		return name;
	}

	private static String[] CreateValidHexSnippet(int length) {
		String[] snippet = new String[length + 1];
		int i;
		for (i = 0; i < length;) {
			snippet[i++] = getRandomString();

		}
		snippet[i] = ":00000001FF"; // EOF
		return snippet;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String[] snippet = new String[5];
		snippet[0] = ":10012000194E79234623965778239EDA3F01B2CAA7";
		snippet[1] = ":100130003F0156702B5E712B722B732146013421C7";
		snippet[2] = ":100130003F0156702B5E712B722B732146013421C7";
		snippet[3] = ":100110002146017E17C20001FF5F16002148011928";
		snippet[4] = ":00000001FF";
//
//		HexFileReader test = new HexFileReader(Arrays.asList(snippet), 1024);
//		test.Parse();

		String[] snippetTrue = new String[3];
		snippetTrue[0] = ":080000000102030405060708D4"; // write 8 bytes (1,2,3,4,5,6,7,8) starting from address 0
		snippetTrue[1] = ":080010000102030405060708C4"; // write 8 bytes (1,2,3,4,5,6,7,8) starting from address 16
		snippetTrue[2] = ":00000001FF";

		HexFileReader test = new HexFileReader(Arrays.asList(snippetTrue), 32);
		MemoryBlock memoryBlock = test.Parse();
		MemoryCell[] memoryCells = memoryBlock.getCells();
//		for (MemoryCell memoryCell : memoryCells) {
//			memoryCell.getValue();
//		}
		for (int i = 0; i < 8; i++) {
			if (i > 0)
				System.out.print("-");
			System.out.print(memoryCells[i].getModified());
		}

		System.out.println();
		for (int i = 8; i < 16; i++) {
			if (i > 0)
				System.out.print("-");
			System.out.print(memoryCells[i].getModified());
		}

		System.out.println();
		for (int i = 16; i < 24; i++) {
			if (i > 0)
				System.out.print("-");
			System.out.print(memoryCells[i].getModified());
		}

		System.out.println();
		for (int i = 24; i < 32; i++) {
			if (i > 0)
				System.out.print("-");
			System.out.print(memoryCells[i].getModified());
		}

		System.out.println();

		for (int i = 0; i < memoryCells.length; i++) {
			if (i % 8 == 0)
				System.out.println();
			if (i > 0)
				System.out.print("-");
			System.out.print(String.format("%02X", memoryCells[i].getValue()));
		}

	}

}
