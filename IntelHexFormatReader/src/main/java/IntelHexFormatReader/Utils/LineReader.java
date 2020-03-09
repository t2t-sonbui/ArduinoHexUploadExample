package IntelHexFormatReader.Utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents the lines found in an {@link InputStream}. The lines are read
 * one at a time using {@link BufferedReader#readLine()} and may be streamed
 * through an iterator or returned all at once.
 *
 * <p>This class does not handle any concurrency issues.
 *
 * <p>The stream is closed automatically when the for loop is done :)
 *
 * <pre>{@code
 * for(String line : new LineReader(stream))
 *      // ...
 * }</pre>
 *
 * <p>An {@link IllegalStateException} will be thrown if any {@link IOException}s
 * occur when reading or closing the stream.
 *
 * @author    Torleif Berger
 * @license   http://creativecommons.org/licenses/by/3.0/
 * @see       http://www.geekality.net/?p=1614
 */
public class LineReader implements Iterable<String>, Closeable
{
    private BufferedReader reader;


    /**
     * Creates a new {@link LineReader}.
     *
     * <p>Uses a {@link FileReader} to read the file.
     *
     * @param file Path to file with lines to read.
     * @throws FileNotFoundException
     */
    public LineReader(String file) throws FileNotFoundException
    {
        this(new FileReader(file));
    }

    /**
     * Creates a new {@link LineReader}.
     *
     * @param stream The {@link Reader} containing the lines to read.
     */
    public LineReader(Reader reader)
    {
        this.reader = new BufferedReader(reader);
    }

    /**
     * Creates an empty {@link LineReader} with no content.
     */
    public LineReader()
    {
        this(new StringReader(""));
    }

    /**
     * Closes the underlying stream.
     */
    @Override
    public void close() throws IOException
    {
        reader.close();
    }

    /**
     * Makes sure the underlying stream is closed.
     */
    @Override
    protected void finalize() throws Throwable
    {
        close();
    }


    /**
     * Returns an iterator over the lines remaining to be read.
     *
     * <p>The underlying stream is closed automatically once {@link Iterator#hasNext()}
     * returns false, so closing it manually after using a for loop shouldn't be necessary.
     *
     * @return This iterator.
     */
    @Override
    public Iterator<String> iterator()
    {
        return new LineIterator();
    }

    /**
     * Returns all lines remaining to be read and closes the stream.
     *
     * @return The lines read from the stream.
     */
    public Collection<String> readLines()
    {
        Collection<String> lines = new ArrayList<String>();
        for(String line : this)
        {
            lines.add(line);
        }
        return lines;
    }

    private class LineIterator implements Iterator<String>
    {
        private String nextLine;

        public String bufferNext()
        {
            try
            {
                return nextLine = reader.readLine();
            }
            catch (IOException e)
            {
                throw new IllegalStateException("I/O error while reading stream.", e);
            }
        }


        public boolean hasNext()
        {
            boolean hasNext = nextLine != null || bufferNext() != null;

            if ( ! hasNext)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    throw new IllegalStateException("I/O error when closing stream.", e);
                }

            return hasNext;
        }

        public String next()
        {
            if ( ! hasNext())
                throw new NoSuchElementException();

            String result = nextLine;
            nextLine = null;
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
