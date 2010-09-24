package de.vwag.kayak.can;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class LogFrameReceiver implements FrameReceiver {
	Boolean gzipped;
	BufferedWriter writer;
	
	public Boolean getGzipped() {
		return gzipped;
	}
	
	
	public LogFrameReceiver(File file) throws IOException {
		FileOutputStream fileStream = new FileOutputStream(file);
		
		if(file.getName().endsWith(".gz")) {
			GZIPOutputStream zipStream = new GZIPOutputStream(fileStream);
			OutputStreamWriter outputWriter = new OutputStreamWriter(zipStream);
			writer = new BufferedWriter(outputWriter);
		} else {
			OutputStreamWriter outputWriter = new OutputStreamWriter(fileStream);
			writer = new BufferedWriter(outputWriter);
		}
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
