package de.vwag.kayak.can;

/**
 * A FrameSource is an interface for objects that can produce CAN frames like a
 * bus adapter or a replay logfile.
 * The source can provide as many bus connections as possible.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public interface FrameSource {
	
	public void connectBus(Bus bus, int number);
	public void open();
	public void close();
	public int getNumberOfBusses();
	

}
