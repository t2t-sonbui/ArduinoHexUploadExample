package IntelHexFormatReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import IntelHexFormatReader.Model.*;

public final class HexFileLineParser {
	private static final String COLON = ":";

	/**
	 * Parse a line in an Intel Hex file.
	 * <p>
	 * A record consists of six parts:
	 * <p>
	 * 1. Start code, one character (':'). 2. Byte count, two hex digits. 3.
	 * Address, four hex digits. 4. Record type, two hex digits (00 to 05). 5. Data,
	 * a sequence of n bytes of data. 6. Checksum, two hex digits.
	 *
	 * @param line A record (line of text) to parse.
	 * @return
	 */
	public static IntelHexRecord ParseLine(String line) throws IOException {
		if (line == null) {
			throw new IOException("Line to parse can not be null");
		}

		// At a minimum, a record should consist of start code (1 char), byte count (2
		// chars), adress (4 chars),
		// record type (2 chars), checksum (2 chars) - only the data part can
		// potentially be empty. This means
		// the line should contain at least 11 characters, or should be deemed too
		// short.
		if (line.length() < 11) {
			throw new IOException(String.format("Line '%1$s' is too short!", line));
		}

		// First character should be a colon.
		if (!line.startsWith(COLON)) {
			throw new IOException(String.format("Illegal line start character ('%1$s'!", line));
		}

		// Parse byteCount, and then calculate and verify required record length
		int byteCount = TryParseByteCount(line.substring(1, 3));
		int requiredRecordLength = 1 + 2 + 4 + 2 + (2 * byteCount) + 2; // checksum

		if (line.length() != requiredRecordLength) {
			throw new IOException(String.format("Line '%1$s' does not have required record length of %2$s!", line,
					requiredRecordLength));
		}

		// Parse address
		int address = TryParseAddress(line.substring(3, 7));

		// Parse record type

		int recTypeVal = TryParseRecordType(line.substring(7, 9));
//        if (!Enum.IsDefined(typeof(RecordType), recTypeVal))
		if (!RecordType.IsDefined(recTypeVal)) {
			throw new IOException(String.format("Invalid record type value: '%1$s'!", recTypeVal));
		}
		RecordType recType = RecordType.forValue(recTypeVal);
		// Parse bytes
		byte[] bytes = TryParseBytes(line.substring(9, 9 + 2 * byteCount));

//		System.out.println("bytes:"+Arrays.toString(bytes));
//		for (int i = 0; i < bytes.length; i++) {
//			if (i > 0)
//				System.out.print("-");
//			System.out.print(String.format("%02X", bytes[i]));
//		}
//		System.out.println();
		// Parse checksum
		int checkSum = TryParseCheckSum(csharpstyle.StringHelper.substring(line, 9 + (2 * byteCount), 2));
//		System.out.println("checkSum:"+checkSum);
		// Verify
		if (!VerifyChecksum(line, byteCount, checkSum)) {
			throw new IOException(String.format("Checksum for line %1$s is incorrect!", line));
		}

		IntelHexRecord tempVar = new IntelHexRecord();
		tempVar.setByteCount(byteCount);
		tempVar.setAddress(address);
		tempVar.setRecordType(recType);
		tempVar.setBytes(bytes);
		tempVar.setCheckSum(checkSum);
		return tempVar;
	}

	private static int TryParseByteCount(String hexByteCount) throws IOException {
		try {
			return Integer.parseInt(hexByteCount, 16);
		} catch (RuntimeException ex) {
			throw new IOException(String.format("Unable to extract byte count for '%1$s'.", hexByteCount), ex);
		}
	}

	private static int TryParseAddress(String hexAddress) throws IOException {
		try {
			return Integer.parseInt(hexAddress, 16);
		} catch (RuntimeException ex) {
			throw new IOException(String.format("Unable to extract address for '%1$s'.", hexAddress), ex);
		}
	}

	private static int TryParseRecordType(String hexRecType) throws IOException {
		try {
			return Integer.parseInt(hexRecType, 16);
		} catch (RuntimeException ex) {
			throw new IOException(String.format("Unable to extract record type for '%1$s'.", hexRecType), ex);
		}
	}

	private static byte[] TryParseBytes(String hexData) throws IOException {
		try {

			byte[] bytes = new byte[hexData.length() / 2];
			int counter = 0;
			for (String hexByte : Split(hexData, 2)) {
				bytes[counter++] = (byte) Integer.parseInt(hexByte, 16);
			}
			return bytes;
		} catch (RuntimeException ex) {
			throw new IOException(String.format("Unable to extract bytes for '%1$s'.", hexData), ex);
		}
	}

	private static int TryParseCheckSum(String hexCheckSum) throws IOException {
		try {
			return Integer.parseInt(hexCheckSum, 16);
		} catch (RuntimeException ex) {
			throw new IOException(String.format("Unable to extract checksum for '%1$s'.", hexCheckSum), ex);
		}
	}

	private static boolean VerifyChecksum(String line, int byteCount, int checkSum) {
		byte[] allbytes = new byte[5 + byteCount];
		int counter = 0;
		for (String hexByte : Split(line.substring(1, 1 + (4 + byteCount) * 2), 2)) {
			allbytes[counter++] = (byte) (Integer.parseInt(hexByte, 16));
		}
//C#    int maskedSumBytes = allbytes.Sum(x -> (short) x) & 0xff;
		int maskedSumBytes = 0;
		for (int i = 0; i < allbytes.length; i++) {
			maskedSumBytes = maskedSumBytes + (short) allbytes[i];
		}
		maskedSumBytes = maskedSumBytes & 0xff;
		int checkSumCalculated = (byte)(256 - maskedSumBytes) &0xff;
		return checkSumCalculated == checkSum;
	}


	// private static IEnumerable<string> Split(string str, int chunkSize)
//    {
//        return Enumerable.Range(0, str.Length / chunkSize)
//                .Select(i => str.Substring(i * chunkSize, chunkSize));
//    }

	private static Iterable<String> Split(String str, int chunkSize) {
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < str.length() / chunkSize; i++) {
			strings.add(str.substring(i * chunkSize, i * chunkSize + chunkSize));
		}
		return strings;
	}

	private static Iterable<String> splitEqually(String str, int chunkSize) {// Se lay ca cai cuoi

		List<String> strings = new ArrayList<String>();
		int index = 0;
		while (index < str.length()) {
			strings.add(str.substring(index, Math.min(index + chunkSize, str.length())));
			index += chunkSize;
		}

		return strings;
	}

}