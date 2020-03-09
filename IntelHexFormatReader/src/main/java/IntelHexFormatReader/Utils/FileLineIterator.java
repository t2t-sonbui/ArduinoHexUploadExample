package IntelHexFormatReader.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class FileLineIterator implements Iterator<String> {
    private FileReader reader;
    private BufferedReader in = null;
    private String string = null;

    public FileLineIterator(File file)
            throws IOException {
        try {
            reader = new FileReader(file);
            in = new BufferedReader(reader);
            string = in.readLine();
            if (string == null) {
                in.close();
                in = null;
            }
        } catch (IOException ex) {
            string = null;
            if (in != null) try {
                in.close();
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
            in = null;
            throw ex;
        }
    }


    @Override
    public boolean hasNext() {
        return string != null;
    }

    @Override
    public String next()
            throws NoSuchElementException {
        String returnString = string;
        try {
            if (string == null) {
                throw new NoSuchElementException("Next line is not available");
            } else {
                string = in.readLine();
                if (string == null && in != null) {
                    in.close();
                    in = null;
                }
            }
        } catch (Exception ex) {
            throw new NoSuchElementException("Exception caught in FileLineIterator.next() " + ex);
        }
        return returnString;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("FileLineIterator.remove() is not supported");
    }

    @Override
    protected void finalize()
            throws Throwable {
        try {
            string = null;
            if (in != null) try {
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            in = null;
        } finally {
            super.finalize();
        }
    }
}
