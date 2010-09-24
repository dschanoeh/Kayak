package de.vwag.kayak.can;

import java.util.ArrayList;
import java.util.Date;

/**
 * A Bus is the virtual representation of a CAN bus. It connects different
 * FrameSources and FrameReceivers for message transport. It also sets the timestamp
 * of each frame to synchronize the message flow.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class Bus {
	ArrayList<FrameReceiver> receivers;
	BusStatistics statistics;
	String name;
	Date startTime;
	
	public String getName() {
		return name;
	}
	
	public void open() {
		startTime = new Date();
	}

	public void setName(String name) {
		this.name = name;
	}

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
		if(name!=null)
			frame.setBusName(name);
		
		/* TODO it needs to be checked if this is the fastest way to set the timestamp.
		 * probably a separate thread could handle this more elegant...
		 */
		if(startTime!=null) {
			frame.setTimestamp(new Date().getTime() - startTime.getTime());
		}
		
		for(FrameReceiver r : receivers) {
			r.newFrame(frame);
		}
		
		if(statistics!=null) {
			statistics.frameReceived(frame.getIdentifier(), frame.getLength());
		}
	}
	
}
