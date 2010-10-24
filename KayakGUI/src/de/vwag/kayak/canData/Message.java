package de.vwag.kayak.canData;

public class Message {
	private String name;
	private String sender;
	private int identifier;
	
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

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	private Signal[] signals;
	
	public Message(MessageInformation information) {
		this.name = information.getName();
		this.sender = information.getSender();
		this.identifier = information.getID();
	}
	
	public void setSignals(Signal[] signals) {
		this.signals = signals;
	}
	
	public Signal[] getSignals() {
		return signals;
	}
}
