package de.vwag.kayak.canData;

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
	
	private Message parse(Frame frame, MessageInformation information) {
		Message message = new Message(information);
		
		ArrayList<SignalInformation> signalInf = information.getSignals();
		Signal[] signals = new Signal[signalInf.size()];
		int i=0;
		
		for(SignalInformation signalInformation : signalInf) {
			signals[i] = new Signal(signalInformation);
			
			/* get the bits and build a value. We have to take care
			 * if this is big or little endian.
			 */
			byte[] data = frame.getData();
			
			
			i++;
		}
		message.setSignals(signals);
		
		return message;
	}
	
	
}
