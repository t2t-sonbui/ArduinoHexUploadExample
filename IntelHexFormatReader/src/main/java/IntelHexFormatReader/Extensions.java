package IntelHexFormatReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import IntelHexFormatReader.Model.*;
import csharpstyle.Func;

public final class Extensions {

	/**
	 * Verify an assertion about an IntelHexRecord, and throw an IOException if the
	 * predicate is not true.
	 *
	 * @param record    The record to verify the assertion for.
	 * @param predicate The assertion to verify.
	 * @param message   The message to show in the exception if the assertion fails.
	 */

	public static void Assert(IntelHexRecord record, Func<IntelHexRecord, Boolean> predicate, String message)
			throws IOException {
		if (!predicate.invoke(record)) {
			throw new IOException(String.format("%1$s -- record %2$s!", message, record));
		}
	}

	/**
	 * Returns the index of the last item in the list for which a certain predicate
	 * is true.
	 * <p>
	 * Returns -1 when no item is found for which the predicate is true.
	 *
	 * @param source    The list of cells to consider.
	 * @param predicate The predicate to test.
	 * @return
	 */
	public static int LastIndexOf(MemoryCell[] source, Func<MemoryCell, Boolean> predicate) {

//		List<MemoryCell> memoryCells = Arrays.asList(source);
//		Collections.reverse(memoryCells);// Dao nguoc
//		MemoryCell[] reversedTemp = memoryCells.toArray(new MemoryCell[source.length]);
//		Collections.reverse(memoryCells);// Dao nguoc de tra ve ban dau
		// Viet lai bang cach khac
		MemoryCell[] reversedTemp = new MemoryCell[source.length];
		for(int i=0;i<source.length;i++)
		{
			reversedTemp[i]=source[source.length-i-1];
		}
		int index = reversedTemp.length - 1;
		for (MemoryCell item : reversedTemp) {
//			System.out.println("index:" + index + "----" + "item:" + String.format("%02X", item.getValue()));
			if (predicate.invoke(item)) {

				return index;
			}
			index--;
		}
		return -1;

	}

}