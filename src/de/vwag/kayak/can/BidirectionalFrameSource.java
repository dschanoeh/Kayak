package de.vwag.kayak.can;

/**
 * A BidirectionalFrameSource extends the FrameSource with methods that enable a
 * bus to send frames back into the frame source. This is used for CAN adapters that
 * can work in a bidirectional way.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public interface BidirectionalFrameSource extends FrameSource {
	
	public void sendFrame(Frame frame);
	
}
