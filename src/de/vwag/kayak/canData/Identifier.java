package de.vwag.kayak.canData;

import de.vwag.kayak.can.*;

public class Identifier {
	private boolean multiplexed;
	
	public boolean isMultiplexed() {
		return multiplexed;
	}
	
	public void setMultiplexed(Boolean multiplexed) {
		this.multiplexed = multiplexed;
	}
	
	public Data[] generateData(Frame frame) {
		return null;
		
	}
}
