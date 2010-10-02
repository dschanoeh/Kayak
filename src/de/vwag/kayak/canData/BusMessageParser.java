package de.vwag.kayak.canData;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Hashtable;

import de.vwag.kayak.can.*;

public class BusMessageParser implements FrameReceiver {
	
	Hashtable<Integer, MessageInformation> messageInformation;
	
	public void setMessageInformation(ArrayList<MessageInformation> information) {
		for(MessageInformation info : information) {
			messageInformation.put(info.getID(), info);
		}
	}
	
	@Override
	public void newFrame(Frame frame) {
		MessageInformation information = messageInformation.get(frame.getIdentifier());
		
		/* If we have no information about the frame we don't need to
		 * parse it.
		 */
		if(information==null)
			return;
		
		/* TODO possibly we could check here if there is someone listening for
		 * this messages. If no we won't have to parse it at all.
		 */
		
		parse(frame, information);
	}
	
	public Message parse(Frame frame, MessageInformation information) {
		Message message = new Message(information);
		
		ArrayList<SignalInformation> signalInf = information.getSignals();
		Signal[] signals = new Signal[signalInf.size()];
		int i=0;
		
		for(SignalInformation signalInformation : signalInf) {
			signals[i] = new Signal(signalInformation);
			
			/* get the bits and build a value. We have to take care
			 * if this is big or little endian.
			 */
			
			long rawData = extractBits(frame.getData(), signalInformation.getStartPosition(), signalInformation.getSize());
			
			/* If the byte order is little endian we have to do some work to switch the bytes.
			 * if it is big endian the order is already correct.
			 */
			if(signalInformation.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
				/* TODO implement little endian */
			}
			double factor = signalInformation.getFactor();
			double offset = signalInformation.getOffset();
			
			long longValue = rawData * (long) factor + (long) offset;
			double doubleValue = (double) rawData * factor + offset;
			
			/* TODO handle maximum and minimum or at least print warnings if the value exceeds
			 * maximum and minimum
			 */
			
			signals[i].setValueDouble(doubleValue);
			signals[i].setValueLong(longValue);
			
			i++;
		}
		message.setSignals(signals);
		
		return message;
	}
	
	/**
	 * This function extracts a given number of bits out of a byte array starting at a
	 * given position. The result is returned as a long.
	 * There are two issues with this approach. First we can not handle values that are
	 * wider than 2^63bit (because long is signed). This is possibly rarely used but
	 * nevertheless should be somehow fixed. Second the performance could be poor
	 * because calculation is done bitwise.
	 * @param data The byte array to work with
	 * @param startBit The first bit to be extracted
	 * @param size The number of bits that have to be extracted
	 * @return Long representation of the extracted bits
	 */
	private long extractBits(byte[] data, int startBit, int size) {
		long val = 0;
		
	     for(int i = 0; i< size;i++){
	         int bitNr = i + startBit;
	         val |= ((data[bitNr >> 3] >> (bitNr & 0x07)) & 1) << i;
	     }
		
		return val;
	}
	
	
}
