/**
 * 	This file is part of Kayak.
 *
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.kayak.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A buffered line reader for files. The position of the last read line can
 * be requested.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class BufferedLineReader {

    private static final int BUFFLEN = 1024*1024;

    private RandomAccessFile newFile = null;
    private byte[] buffer = new byte[BUFFLEN];
    private long bufferBasePosition; /* Position of the buffer base in the file */
    private String string;
    private int start=0; /* Position of the next line in the string */
    private int bufferFill=0; /* Number of valid bytes in the buffer */
    private long positionOfLastLine;

    public BufferedLineReader(File file, long pos) throws FileNotFoundException, IOException {

        newFile = new RandomAccessFile(file, "r");
        newFile.seek(pos);
    }

    public long getPositionOfLastLine() {
        return positionOfLastLine;
    }

    public void close() throws IOException {
        newFile.close();
    }

    public void seek(long pos) throws IOException {
        newFile.seek(pos);

        /* after a seek the buffer must be filled */
        bufferBasePosition = pos;
        start = 0;
        int read = newFile.read(buffer, 0, BUFFLEN);
        bufferFill = read;
        string = new String(buffer, "ASCII");
    }

    public String readLine() throws IOException {
        /* Is there a full line after start? */
        int stop=-1;
        for(int i=start;i<bufferFill;i++) {
            if(buffer[i] ==(byte)'\n') {
                stop = i;
                break;
            }
        }

        /* There is no new line. Fill buffer */
        if(stop == -1) {
            /* Copy end of line to beginning */
            for(int i=start;i<bufferFill;i++) {
                buffer[i-start] = buffer[i];
            }
            bufferFill = bufferFill - start;

            /* Fill rest of buffer */
            long positionBeforeRead = newFile.getFilePointer();
            bufferBasePosition = positionBeforeRead - bufferFill;
            int read = newFile.read(buffer, bufferFill, BUFFLEN-bufferFill);

            bufferFill += read;

            string = new String(buffer, "ASCII");

            start = 0;

            /* Try again to find a newline */
            for(int i=start;i<bufferFill;i++) {
                if(buffer[i] ==(byte)'\n') {
                    stop = i;
                    break;
                }
            }

            /* Still no newline */
            if(stop == -1) {
                /* End of file */
                if(start >= bufferFill) {
                    return null;
                } else {
                    stop = bufferFill;
                }
            }
        }

        /*
         * At this position it is safe that there is a full line between
         * start and stop
         */
        String line = string.substring(start, stop);
        positionOfLastLine = bufferBasePosition + start;
        start = stop+1;
        return line;
    }

}
