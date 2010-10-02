package de.vwag.kayak.can;

import java.util.ArrayList;

/**
 * A Bus is the virtual representation of a CAN bus. It connects different
 * FrameSources and FrameReceivers for message transport. It also sets the timestamp
 * of each frame to synchronize the message flow.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class Bus {
	private ArrayList<FrameReceiver> receivers;
	private BusStatistics statistics;
	private String name;
	private TimeSource timeSource;
	
	public String getName() {
		return name;
	}

	public TimeSource getTimeSource() {
		return timeSource;
	}

	public void setTimeSource(TimeSource timeSource) {
		this.timeSource = timeSource;
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
	
	public void unregisterReceiver(FrameReceiver receiver) {
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
		
		if(timeSource != null)
			frame.setTimestamp(timeSource.getTime());
		
		for(FrameReceiver r : receivers) {
			r.newFrame(frame);
		}
		
		if(statistics!=null) {
			statistics.frameReceived(frame.getIdentifier(), frame.getLength());
		}
	}
	
}
