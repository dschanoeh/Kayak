package de.vwag.kayak.can;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * A ReplayFrameSource is an implementation of a unidirectional FrameSource.
 * It is used to replay a saved logfile.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class ReplayFrameSource implements FrameSource, Runnable{
	private Boolean stop=false;
	private BufferedReader reader;
	private ArrayList<String> busNames;
	private Bus[] busses;
	private Thread myThread;
	private File file;
	private String description;
	private String platform;
	private long timeOffset;
	private Logger logger = Logger.getLogger("de.vwag.kayak.can");
	
	public ReplayFrameSource(File file) throws FileNotFoundException, IOException {
		this.file = file;
		reOpenFile(file);
		
		busNames = new ArrayList<String>();
		Boolean firstLine = true;
		
		while(reader.ready()) {
			String line = reader.readLine();
			
			String bus="";
			
			/* Ignore empty lines */
			if(line != "") {
				if(line.startsWith("DEVICE_ALIAS")) {
					/* TODO to be implemented */
				} else if(line.startsWith("PLATFORM")) {
					platform = line.split("\\s")[1];
				} else if(line.startsWith("DESCRIPTION")) {
					description = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")-1);
				} else if(line.startsWith("(")){
					bus = line.split("\\s")[1];
					
					if(!busNames.contains(bus)) {
						busNames.add(bus);
					}
					
					if(firstLine) {
						String[] cols = line.split("\\s");
						timeOffset = (long)(Double.parseDouble((cols[0].substring(1, cols[0].length()-1))) * 1000);
						firstLine = false;
					}
				}
			}
		}
		busses = new Bus[busNames.size()];
		
		reOpenFile(file);	
	}
	
	public String getDescription() {
		if(platform==null)
			return description;
		else
			return description + " (" + platform + ")";
	}
	
	public String getNameOfBus(int number) {
		return busNames.get(number);
	}
	
	private void reOpenFile(File file) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(file);
		if(file.getName().endsWith(".gz")) {
			GZIPInputStream zipstream = new GZIPInputStream(fis);
			InputStreamReader isr = new InputStreamReader(zipstream);
			reader = new BufferedReader(isr);
		} else {
			InputStreamReader isr = new InputStreamReader(fis);
			reader = new BufferedReader(isr);
		}
	}
	
	public void close() {
		stop = true;
	}

	public void open() {
		myThread = new Thread(this);
		myThread.start();
	}

	public void connectBus(Bus bus, int number) {
		busses[number] = bus;
	}

	public int getNumberOfBusses() {
		return busNames.size();
	}

	public void run() {
		try {		
			for(;;) {
				long startTime = System.currentTimeMillis();
				
				while(reader.ready() && !stop) {
					String line = reader.readLine();
					if(!line.startsWith("("))
						continue;
					
					String[] cols = line.split("\\s");
					
					/* check if we have a bus connected for this recorded bus */
					int busNumber = busNames.indexOf(cols[1]);
					if(busNumber < 0 || busses[busNumber] == null)
						continue;
					
					long msecs = (long)(Double.parseDouble((cols[0].substring(1, cols[0].length()-1))) * 1000) - timeOffset;
					String[] data = cols[2].split("#");
					
					int identifier = Integer.parseInt(data[0], 16);
					byte[] message = Util.hexStringToByteArray(data[1]);
					
					Frame frame = new Frame(identifier, message);
					
					long timeToWait = msecs - (System.currentTimeMillis() - startTime);
					
					/* if timeToWait is <0 we are to late. if it is >0 we have to wait. This only makes sense if
					 * it is more than a few ms.
					 */
					if(timeToWait >= 10) {
						Thread.sleep(timeToWait);
					} 
					
					busses[busNumber].receiveFrame(frame);
				}
				
				if(stop)
					break;
				
				reader.close();
				reOpenFile(file);
				logger.log(Level.INFO, "Restarting replay of logfile.");
			}	
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO error while reading logfile.", e);
			return;
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "Logfile replay thread was interruped.", e);
		}	
	}
}