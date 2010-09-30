package de.vwag.kayak.canData;

public class Message {
	private String name;
	private String sender;
	private int identifier;
	
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
