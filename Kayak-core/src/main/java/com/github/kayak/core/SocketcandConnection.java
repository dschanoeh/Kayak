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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This abstract class provides some common methods that are necessary for
 * socketcand connections. Both {@link BCMConnection} and {@link RAWConnection}
 * extend this class.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public abstract class SocketcandConnection {

    private static final int BUFFER_SIZE = 4096;
    private static final int ELEMENT_SIZE = 512;

    private static final Logger logger = Logger.getLogger(SocketcandConnection.class.getCanonicalName());

    protected String busName;
    protected int port;
    protected String host;
    private FrameListener receiver;
    private BufferedReader reader;
    private final char[] elementBuffer = new char[ELEMENT_SIZE];

    public FrameListener getListener() {
        return receiver;
    }

    public void setListener(FrameListener receiver) {
        this.receiver = receiver;
    }

    public String getBusName() {
        return busName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    protected void setInput(InputStreamReader stream) {
        reader = new BufferedReader(stream, BUFFER_SIZE);
    }

    /**
     * This method reads data from an {@link InputStreamReader} and tries to
     * extract elements that are enclosed by '<' and '>'. The Reader must be set
     * via setInput() before calling getElement().
     * A buffer is used to
     * construct elements that need multiple reads. Whitespace between the
     * elements is ignored.
     * The method blocks until an element can be returned.
     * @param in the InputStreamReader from which should be read
     * @return the first element read
     * @throws IOException
     */
    protected String getElement() throws IOException, InterruptedException, SocketTimeoutException {
        int pos = 0;
        boolean inElement = false;

        while (true) {
            char c = (char) reader.read();

            /* Find opening < */
            if (!inElement) {
                if (c == '<') {
                    inElement = true;
                    elementBuffer[pos] = c;
                    pos++;
                }
            } else {
                if(pos >= ELEMENT_SIZE-1) { /* Handle large elements */
                    logger.log(Level.WARNING, "Found frame that is too large. Ignoring...");
                    pos = 0;
                    inElement = false;

                } else if (c == '>') { /* Find closing > */
                    elementBuffer[pos] = c;
                    pos++;
                    break;
                } else { /* Element content */
                    elementBuffer[pos] = c;
                    pos++;
                }
            }
        }

        return String.valueOf(elementBuffer, 0, pos);
    }
}
