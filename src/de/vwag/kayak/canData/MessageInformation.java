package de.vwag.kayak.canData;

import java.util.ArrayList;

public class MessageInformation {
	private boolean multiplexed;
	private int ID;
	private ArrayList<SignalInformation> signals;
	private String name;
	private String sender;
	private int length;
	
	public MessageInformation() {
		signals = new ArrayList<SignalInformation>();
	}
	
	public ArrayList<SignalInformation> getSignals() {
		return signals;
	}
	
	public void addSignal(SignalInformation signal) {
		signals.add(signal);
	}
	
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
	public boolean isMultiplexed() {
		return multiplexed;
	}
	
	public void setMultiplexed(Boolean multiplexed) {
		this.multiplexed = multiplexed;
	}
}
