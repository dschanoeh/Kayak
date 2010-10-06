package de.vwag.kayak.canData;

import java.nio.ByteOrder;

import org.junit.Test;

import de.vwag.kayak.can.Frame;
import de.vwag.kayak.can.Util;

import junit.framework.TestCase;

public class BusMessageParserTest extends TestCase {
	BusMessageParser parser;
	Frame frame;
	MessageInformation mi;
	
	public void setUp() {
		parser = new BusMessageParser();
		
		frame = new Frame();
		byte[] data = Util.hexStringToByteArray("09030F");
		frame.setData(data);
		mi = new MessageInformation();
		
		SignalInformation i = new SignalInformation();
		i.setByteOrder(ByteOrder.BIG_ENDIAN);
		i.setStartPosition(14);
		i.setSize(9);
		i.setFactor(1);
		i.setOffset(0);
		mi.addSignal(i);
		
		SignalInformation j = new SignalInformation();
		j.setByteOrder(ByteOrder.BIG_ENDIAN);
		j.setStartPosition(0);
		j.setSize(8);
		j.setFactor(1);
		j.setOffset(0);
		mi.addSignal(j);
		
		SignalInformation k = new SignalInformation();
		k.setByteOrder(ByteOrder.BIG_ENDIAN);
		k.setStartPosition(12);
		k.setSize(8);
		k.setFactor(1);
		k.setOffset(0);
		k.setSigned(true);
		mi.addSignal(k);
	}
	
	@Test
	public void testParser() {
		Message message = parser.parse(frame, mi);
		Signal[] signals = message.getSignals();
		assertEquals("Testing value of first signal",60,signals[0].getValueLong());
		assertEquals("Testing value of first signal",9,signals[1].getValueLong());
		assertEquals("Testing value of first signal",-16,signals[2].getValueLong());
	}
	
	

}
