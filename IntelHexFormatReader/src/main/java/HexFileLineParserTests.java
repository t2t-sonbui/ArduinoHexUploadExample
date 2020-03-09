import java.io.IOException;
import java.util.Arrays;

import IntelHexFormatReader.HexFileLineParser;
import IntelHexFormatReader.Model.IntelHexRecord;

public class HexFileLineParserTests {

	public static void HexFileLineParserThrowsExceptionLineIsWhenNull() throws IOException {
		HexFileLineParser.ParseLine(null);
		// nullArgAction.ShouldThrow<IOException>().WithMessage("*can not be null*");
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// HexFileLineParser.ParseLine(null);
//		HexFileLineParser.ParseLine("");
//		HexFileLineParser.ParseLine(":000010015");
		String invalidRecord1 = ":100130003F015672B5E712B722B732146013421C7"; // one char too short
		String invalidRecord2 = ":100130003F0156702B5E712B722B7321460133421C7"; // one char too long

		// HexFileLineParser.ParseLine(invalidRecord1);
//		HexFileLineParser.ParseLine(":00000001F1");

		   String line1 = ":10010000214601360121470136007EFE09D2190140";
           String line2 = ":100130003F0156702B5E712B722B732146013421C7";
           String line3 = ":00000001FF";

		IntelHexRecord record = HexFileLineParser.ParseLine(line2);
		System.out.println(record.getRecordType());
		System.out.println(record.getAddress());
		System.out.println(record.getByteCount());
		System.out.println(record.getCheckSum());
		byte[] byteCheck = record.getBytes();
		for (int i = 0; i < record.getBytes().length; i++) {
			if (i > 0)
				System.out.print("|");
			System.out.print(String.valueOf(byteCheck[i]));
		}

	}

}
