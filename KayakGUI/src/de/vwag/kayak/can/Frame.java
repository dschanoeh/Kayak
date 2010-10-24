package de.vwag.kayak.can;

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
}
