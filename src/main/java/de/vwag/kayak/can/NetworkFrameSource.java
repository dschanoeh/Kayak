package de.vwag.kayak.can;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkFrameSource implements FrameSource, Runnable {
	private Logger logger = Logger.getLogger("de.vwag.kayak.can");
	private Socket socket;
	private String host;
	private int port;
	private Scanner input;
	private PrintWriter output;
	private Thread thread;
	private boolean stopRequest = false;
	private BusNameContainer container;
	public static int ID_ALL = 0;
	
	public NetworkFrameSource(String host, int port) {
		this.host = host;
		this.port = port;
				
		socket = new Socket();
		container = new BusNameContainer();
	}
	
	public void subscribeID(String bus, int id) {
		if(id == ID_ALL) {
			for(int i=0;i<2048;i++) {
				output.write(String.format("< %s F 0 0 %3X 0 >", bus, i));
			}
		} else {
			output.write(String.format("< %s F 0 0 %3X 0 >", bus, id));
		}
		
		output.flush();
	}
	
	public void unsubscribeID(String bus, int id) {
		if(id == ID_ALL) {
			for(int i=0;i<2048;i++) {
				output.write(String.format("< %s X 0 0 %3X 0 >", bus, i));
			}
		} else {
			output.write(String.format("< %s X 0 0 %3X 0 >", bus, id));
		}
		
		output.flush();
	}

	@Override
	public void open() {
		InetSocketAddress address = new InetSocketAddress(host, port);
		
		try {
			socket.connect(address);
			socket.setSoTimeout(1000);
			
			input = new Scanner(socket.getInputStream());
			output = new PrintWriter(socket.getOutputStream(),true);
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException while creating the socket.",e);
		}
		
		/* Start worker thread for frame reception */
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void close() {
		stopRequest = true;
		try {
			thread.join();
		} catch (InterruptedException e) {

		}
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		/* separate frames by '>' followed by optional whitespace or null characters*/
		input.useDelimiter(">[\\s\\000]*");

		while(true) {
			if(stopRequest)
				break;
			
			try {
				String frame = input.next();
				
				String[] fields = frame.split("\\s");
				
				/* We received a frame */
				if(fields[2].equals("f")) {
					try {
						Bus bus = container.getBus(fields[1]);
						if(bus  != null) {
							Frame f = new Frame(Integer.valueOf(fields[2], 16), Util.hexStringToByteArray(fields[3]));
							bus.receiveFrame(f);
						}
						
						
					} catch(Exception ex) {
						logger.log(Level.WARNING, "Could not properly parse CAN frame", ex);
					}
				}
			} catch (NoSuchElementException ex) {
				continue;
			}
		}
	}

	@Override
	public void connectBus(Bus bus, String name) {
		container.addPair(bus, name);
	}

	@Override
	public String[] getBusNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
