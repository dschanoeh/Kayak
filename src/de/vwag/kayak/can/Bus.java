package de.vwag.kayak.can;

import java.util.ArrayList;


public abstract class Bus {
	ArrayList<FrameReceiver> receivers;
	BusStatistics statistics;
	
	public Bus() {
		receivers = new ArrayList<FrameReceiver>();
		statistics = null;
	}
	
	public void registerReceiver(FrameReceiver receiver) {
		if(!receivers.contains(receiver)) {
			receivers.add(receiver);
		}
	}
	
	public void deregisterReceiver(FrameReceiver receiver) {
		receivers.remove(receiver);
	}
	
	public void enableStatistics() {
		statistics = new BusStatistics();
	}
	
	public void disableStatistics() {
		statistics = null;
	}
	
	public void receiveFrame(Frame frame) {
		for(FrameReceiver r : receivers) {
			r.newFrame(frame);
		}
		
		if(statistics!=null) {
			statistics.frameReceived(frame.getIdentifier(), frame.getLength());
		}
	}
	
}
