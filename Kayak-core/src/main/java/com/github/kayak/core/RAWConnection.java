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
 * that bring a socketcand in RAW mode. Frames are delivered asynchronously
 * through an own thread.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class RAWConnection extends SocketcandConnection implements Runnable {
	private Logger logger = Logger.getLogger("com.github.kayak.backend");
	private Socket socket;
	private PrintWriter output;
	private Thread thread;
	private boolean stopRequest = false;
	private InputStreamReader input;

	public RAWConnection(BusURL url) {
		this.host = url.getHost();
		this.port = url.getPort();
		this.busName = url.getName();
				
		socket = new Socket();
	}
	
	public void open() {
		InetSocketAddress address = new InetSocketAddress(host, port);
		
		try {
			socket.connect(address);
			socket.setSoTimeout(1000);
			
			input = new InputStreamReader(
                    socket.getInputStream());
			
			output = new PrintWriter(socket.getOutputStream(),true);
			
			String ret = getElement(input);
			if(!ret.equals("< hi >")) {
				logger.log(Level.SEVERE, "Did not receive greeting from host.");
			}
			
			output.print("< open " + busName + " >");
			output.flush();
			
			ret = getElement(input);
			if(!ret.equals("< ok >")) {
				logger.log(Level.SEVERE, "Could not open bus");
			}
			
			output.print("< rawmode >");
			output.flush();
			
			ret = getElement(input);
			if(!ret.equals("< ok >")) {
				logger.log(Level.SEVERE, "Could not switch to RAW mode.");
			}
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException while creating the socket.",e);
		}
		
		/* Start worker thread for frame reception */
		thread = new Thread(this);
		thread.start();
	}
	
	public void close() {
		stopRequest = true;
		try {
			thread.join();
		} catch (Exception e) {}
		try {
			socket.close();
		} catch (IOException e) {}
	}

	@Override
	public void run() {
		while(true) {
			if(stopRequest)
				break;
			
			try {
				String frame = getElement(input);
				
				String[] fields = frame.split("\\s");
				
				if(fields[1].equals("frame")) {
					try {
                                                String dataString = "";
                                                for(int i=3;i<fields.length;i++) {
                                                    dataString += fields[i];
                                                }
						Frame f = new Frame(Integer.valueOf(fields[2], 16), Util.hexStringToByteArray(dataString));
						FrameReceiver receiver = this.getReceiver();
						if(receiver != null)
							receiver.newFrame(f);
						
					} catch(Exception ex) {
						logger.log(Level.WARNING, "Could not properly deliver CAN frame", ex);
					}
				} else if(fields[1].equals("error")) {
					logger.log(Level.WARNING, "Received error from socketcand: " + frame);
				}
			} catch (IOException ex) {
				
			}
		}
	}
}
