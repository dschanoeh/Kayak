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
import java.net.SocketTimeoutException;

/**
 * This abstract class provides some common methods that are necessary for
 * socketcand connections. Both {@link BCMConnection} and {@link RAWConnection}
 * extend this class. 
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public abstract class SocketcandConnection {
	private static final int BUFFER_SIZE = 128;
	private char[] buffer = new char[BUFFER_SIZE];
	private int bufferPosition=0;
	protected String busName;
	protected int port;
	protected String host;
	private FrameReceiver receiver;

	public FrameReceiver getReceiver() {
		return receiver;
	}

	public void setReceiver(FrameReceiver receiver) {
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
	
	/**
	 * This method reads data from an {@link InputStreamReader} and tries to
	 * extract elements that are enclosed by '<' and '>'. A buffer is used to
	 * construct elements that need multiple reads. Whitespace between the
	 * elements is ignored. 
	 * The method blocks until an element can be returned. 
	 * @param in the InputStreamReader from which should be read
	 * @return the first element read
	 * @throws IOException
	 */
	protected String getElement(InputStreamReader in) throws IOException, InterruptedException, SocketTimeoutException {
		Boolean first = true;
		
		while(true) {
			/* on the first run we check if elements can be retrieved without
			 * reading in more data.
			 */
			if(!(first && bufferPosition > 0))
				bufferPosition += in.read(buffer, bufferPosition, BUFFER_SIZE-bufferPosition);
			
			first = false;
			
			/* locate opening '<' */
			int start=-1;
			if(buffer[0] == '<') {
				start = 0;
			} else {
				for(int i=0;i<bufferPosition;i++) {
					if(buffer[i]=='<')
						start = i;
				}
			}
			
			/* if no '<' is in the string, the string can be discarded */
			if(start==-1) {
				bufferPosition = 0;
				continue;
			}
			
			/* locate first closing '>' */
			int stop=-1;
			for(int i=start+1;i<bufferPosition;i++) {
				if(buffer[i]=='>') {
					stop = i;
					break;
				}
			}
			/* no '>' --> not enough data yet */
			if(stop==-1) {
				continue;
			}
	
			String element = String.copyValueOf(buffer, start, stop-start+1);
			
			if(bufferPosition==(stop+1))
				bufferPosition=0;
			else {
				for(int i=stop+1;i<bufferPosition;i++) {
					buffer[i-stop-1] = buffer[i];
				}
				bufferPosition = bufferPosition-stop-1;
			}
		
			return element;
		}
	}
}
