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

public class Frame {
	private byte[] data;
	private int identifier;
	private String busName;
	private long timestamp;
	
	public String getBusName() {
		return busName;
	}

	public void setBusName(String busName) {
		this.busName = busName;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public Frame() {
		
	}
	
	public Frame(int identifier, byte[] data) {
		this.identifier = identifier;
		this.data = data;
	}
	
	public Frame(int identifier, byte[] data, long timestamp) {
		this.identifier = identifier;
		this.data = data;
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public int getIdentifier() {
		return identifier;
	}
	
	public int getLength() {
		return data.length;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public String toString() {
		String s = "Frame [" + Integer.toHexString(identifier) + "] " + Util.byteArrayToHexString(data);
		return s;
	}
}
