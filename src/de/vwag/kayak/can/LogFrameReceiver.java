package de.vwag.kayak.can;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFrameReceiver implements FrameReceiver {
	FileWriter writer;
	public LogFrameReceiver(File file) throws IOException {
		writer = new FileWriter(file);
	}
	
	public void close() throws IOException {
		writer.close();
	}
	@Override
	public void newFrame(Frame frame) {
		/* TODO The timestamp number formatting is dirty... */
		long timestamp = frame.getTimestamp();
		String s = "(" + String.valueOf((long)(timestamp/1000)) + "." + String.format("%06d", (long)(timestamp % 1000)*100) + ") " + frame.getBusName() + " " + String.format("%03d", frame.getIdentifier()) + "#";
		
		s += Util.byteArrayToHexString(frame.getData());
		
		s += "\n";
		
		try {
			writer.write(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
