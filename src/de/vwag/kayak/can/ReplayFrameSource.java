package de.vwag.kayak.can;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

/**
 * A ReplayFrameSource is an implementation of a unidirectional FrameSource.
 * It is used to replay a saved logfile.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class ReplayFrameSource implements FrameSource, Runnable{
	private File logFile;
	private Boolean stop=false;
	BufferedReader reader;
	ArrayList<String> busNames;
	Bus[] busses;
	Thread myThread;
	File file;
	
	public ReplayFrameSource(File file) throws FileNotFoundException, IOException {
		this.file = file;
		reOpenFile(file);
		
		busNames = new ArrayList<String>();
		
		while(reader.ready()) {
			String line = reader.readLine();
			String bus = line.split("\\s")[1];
			
			if(!busNames.contains(bus)) {
				busNames.add(bus);
			}
		}
		busses = new Bus[busNames.size()];
		
		reOpenFile(file);
		
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
	
	
	@Override
	public void close() {
		stop = true;
	}

	@Override
	public void open() {
		myThread = new Thread(this);
		myThread.start();
	}

	@Override
	public void connectBus(Bus bus, int number) {
		busses[number] = bus;
	}

	@Override
	public int getNumberOfBusses() {
		return busNames.size();
	}

	@Override
	public void run() {
		try {
			Date startTime;
			
			for(;;) {
				startTime = new Date();
				
				while(reader.ready() && !stop) {
					String line = reader.readLine();
					String[] rows = line.split("\\s");
					
					/* check if we have a bus connected for this recorded bus */
					int busNumber = busNames.indexOf(rows[1]);
					if(busNumber < 0 || busses[busNumber] == null)
						continue;
					
					long msecs = (long)(Double.parseDouble((rows[0].substring(1, rows[0].length()-1))) * 1000);
					String[] data = rows[2].split("#");
					int identifier = Integer.parseInt(data[0]);
					byte[] message = Util.hexStringToByteArray(data[1]);
					
					Frame frame = new Frame(identifier, message);
					
					long timeToWait = msecs - ((new Date()).getTime() - startTime.getTime());
					
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
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
