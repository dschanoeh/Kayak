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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A RAWConnection extends the {@link SocketcandConnection} and adds methods
 * that bring a socketcand in BCM mode. Frames are delivered asynchronously
 * through an own thread.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class BCMConnection extends SocketcandConnection implements Runnable {

    private static final Logger logger = Logger.getLogger(BCMConnection.class.getName());
    private Socket socket;
    private PrintWriter output;
    private Thread thread;
    private InputStreamReader input;
    private Boolean connected = false;

    public Boolean isConnected() {
        return connected;
    }

    public BCMConnection(BusURL url) {
        this.host = url.getHost();
        this.port = url.getPort();
        this.busName = url.getBus();
    }

    public void open() {
        InetSocketAddress address = new InetSocketAddress(host, port);

        try {
            socket = new Socket();
            socket.connect(address);
            socket.setSoTimeout(1000);

            input = new InputStreamReader(socket.getInputStream());
            setInput(input);

            output = new PrintWriter(socket.getOutputStream(), true);

            String ret = getElement();
            if (!ret.equals("< hi >")) {
                logger.log(Level.SEVERE, "Did not receive greeting from host.");
            }

            output.print("< open " + busName + " >");
            output.flush();

            ret = getElement();
            if (!ret.equals("< ok >")) {
                logger.log(Level.SEVERE, "Could not open bus");
            }
            socket.setSoTimeout(100);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "IOException while creating the socket.", e);
            return;
        }

        /* Start worker thread for frame reception */
        thread = new Thread(this);
        thread.start();
        connected = true;
    }

    public void close() {
        if (thread != null && thread.isAlive()) {
            try {
                thread.interrupt();
                thread.join();
            } catch (Exception e) {
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
        }
        connected = false;
    }

    public void subscribeTo(int id, int sec, int usec) {
        synchronized(output) {
            output.print("< subscribe "
                    + Integer.toString(sec) + " "
                    + Integer.toString(usec) + " "
                    + Integer.toHexString(id) + " >");
            output.flush();
        }
    }

    public void unsubscribeFrom(int id) {
        synchronized(output) {
            output.print("< unsubscribe " + Integer.toHexString(id) + " >");
            output.flush();
        }
    }

    public void addSendJob(Frame f, int sec, int usec) {
        synchronized(output) {
            output.print("< add "
                    + Integer.toString(sec) + " "
                    + Integer.toString(usec) + " "
                    + Integer.toHexString(f.getIdentifier()) + " "
                    + Integer.toString(f.getLength()) + " "
                    + Util.byteArrayToHexString(f.getData()) + " >");
            output.flush();
        }
    }

    public void updateSendJob(Frame f) {
        synchronized(output) {
            output.print("< add "
                    + Integer.toHexString(f.getIdentifier()) + " "
                    + Integer.toString(f.getLength()) + " "
                    + Util.byteArrayToHexString(f.getData()) + " >");
            output.flush();
        }
    }

    public void sendFrame(Frame f) {
        StringBuilder sb = new StringBuilder();
        sb.append("< send ");
        sb.append(Integer.toHexString(f.getIdentifier()));
        sb.append(' ');
        sb.append(Integer.toString(f.getLength()));
        sb.append(' ');
        String data = Util.byteArrayToHexString(f.getData());
        for(int i=0;i<data.length();i+=2) {
            sb.append(data.charAt(i));
            sb.append(data.charAt(i+1));
            sb.append(' ');
        }
        sb.append(">");
        synchronized(output) {
            output.print(sb.toString());
            output.flush();
        }
    }

    @Override
    public void run() {
        StringBuilder sb;
        
        while (true) {
            if(Thread.interrupted())
                return;

            try {
                String frame = getElement();

                String[] fields = frame.split("\\s");

                /* We received a frame */
                if (fields[1].equals("frame")) {
                    try {
                        sb = new StringBuilder(16);
                        for (int i = 4; i < fields.length-1; i++) {
                            sb.append(fields[i]);
                        }
                        Frame f = new Frame(Integer.valueOf(fields[2], 16), Util.hexStringToByteArray(sb.toString()));
                        int pos = 0;
                        for(;pos<fields[3].length();pos++) {
                            if(fields[3].charAt(pos) =='.')
                                break;
                        }
                        long timestamp = 1000000 * Long.parseLong(fields[3].substring(0, pos)) + Long.parseLong(fields[3].substring(pos+1));
                        f.setTimestamp(timestamp);
                        FrameReceiver receiver = this.getReceiver();
                        if (receiver != null) {
                            receiver.newFrame(f);
                        }

                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "Could not properly deliver CAN frame", ex);
                    }
                } else if (fields[1].equals("error")) {
                    logger.log(Level.WARNING, "Received error from socketcand: {0}", frame);
                }
            } catch(InterruptedException ex) {
                logger.log(Level.WARNING, "Interrupted exception. Shutting down connection thread");
                return;
            } catch (IOException ex) {
                /* 
                 * A read from the socket may time out if there are very few frames.
                 * this will cause an IOException. This is ok so we will ignore these
                 * exceptions
                 */
            }
        }
    }
}
