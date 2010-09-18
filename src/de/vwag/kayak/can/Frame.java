package de.vwag.kayak.can;
import java.util.Date;


public class Frame {
	byte[] data;
	int identifier;
	
	Date timestamp;
	
	public Frame() {
		
	}
	
	public Frame(int identifier, byte[] data) {
		this.identifier = identifier;
		this.data = data;
	}
	
	public Frame(int identifier, byte[] data, Date timestamp) {
		this.identifier = identifier;
		this.data = data;
		this.timestamp = timestamp;
	}
	
	public Date getTimestamp() {
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
}
