package de.vwag.kayak.can;

/**
 * This class is used to synchronize the time between different busses. After a
 * TimeSource is created it can be connected to any number of busses. Each frame
 * passing through the bus will get a timestamp from the TimeSource.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class TimeSource {
	long reference;
	
	public TimeSource() {
		reference = System.currentTimeMillis();
	}
	
	public void reset() {
		reference = System.currentTimeMillis();
	}
	
	public long getTime() {
		return System.currentTimeMillis() - reference;
	}
}
