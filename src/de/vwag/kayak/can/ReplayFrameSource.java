package de.vwag.kayak.can;

import java.io.BufferedReader;
import java.io.File;

/**
 * A ReplayFrameSource is an implementation of a unidirectional FrameSource.
 * It is used to replay a saved logfile.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class ReplayFrameSource implements FrameSource {
	private File logFile;
	private Boolean stop=false;
	
	public ReplayFrameSource(File file) {
		
	}
	
	@Override
	public void close() {
		stop = true;
		
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectBus(Bus bus, int number) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumberOfBusses() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
