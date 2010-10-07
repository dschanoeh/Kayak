package de.vwag.kayak.canData;
import java.io.*;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBCParser {
	ArrayList<MessageInformation> identifiers;
	private static Logger logger = Logger.getLogger("de.vwag.kayak.canData");
	
	public ArrayList<MessageInformation> getIdentifiers() {
		return identifiers;
	}
	
	public void parse(String filename) {
		File file = new File(filename);
		identifiers = new ArrayList<MessageInformation>();
		
		if(!file.canRead())
			return;
		
		try {			
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			
			String line = reader.readLine();
			
			while(reader.ready()) {
				
				if(line.startsWith("BS_")) {
					/* TODO parse baudrate */
					line=null;
				} else if(line.startsWith("BO_")) {
					MessageInformation identifier = new MessageInformation();
					
					/* split at the : */
					String[] split = line.split(":");
					
					/* split by whitespace and extract id and name */
					String[] identifierSplit = split[0].split("\\s");
					identifier.setID(Integer.parseInt(identifierSplit[1]));
					identifier.setName(identifierSplit[2]);
					
					/* split by whitespace and extract size and sender */
					String[] sizeAndSenderSplit = split[1].split("\\s");
					identifier.setLength(Integer.parseInt(sizeAndSenderSplit[1]));
					identifier.setSender(sizeAndSenderSplit[2]);
					
					/* now check the following lines for signals */
					String newLine = reader.readLine();
					
					while(newLine.startsWith(" SG_")) {
						SignalInformation signal = new SignalInformation();

						String[] split1 = newLine.split("\\|");
						
						String[] nameSplit = split1[0].split("\\s");
						signal.setName(nameSplit[2]);
						signal.setStartPosition(Integer.parseInt(nameSplit[4]));
						
						String[] restSplit = split1[1].split("\\s");
						
						/* restSplit[0] is the 5@1+ part */
						String[] sizePart = restSplit[0].split("@");
						signal.setSize(Integer.parseInt(sizePart[0]));
						
						if(sizePart[1].charAt(0) == '0') {
							signal.setByteOrder(ByteOrder.LITTLE_ENDIAN);
						} else if(sizePart[1].charAt(0) == '1') {
							signal.setByteOrder(ByteOrder.BIG_ENDIAN);
						}
						
						if(sizePart[1].charAt(1) == '+') { 
							signal.setSigned(false);
						} else if(sizePart[1].charAt(1) == '-') { 
							signal.setSigned(false);
						}
						
						String[] factorAndOffset = restSplit[1].substring(1, restSplit[1].length()-1).split(",");
						signal.setFactor(Float.parseFloat(factorAndOffset[0]));
						signal.setOffset(Float.parseFloat(factorAndOffset[1]));
						
						signal.setMinimum(Float.parseFloat(restSplit[2].substring(1, restSplit[2].length())));
						
						String[] endSplit = split1[2].split("\\s");
						signal.setMaximum(Float.parseFloat(endSplit[0].substring(0,endSplit[0].length()-1)));
						
						signal.setUnit(endSplit[1].substring(1, endSplit[1].length()-1));
						
						if(!endSplit[endSplit.length-1].equals("Vector__XXX")) {
							if(endSplit[2].contains(",")) {
								String[] receiverSplit = endSplit[endSplit.length-1].split(",");
								signal.setReceivers(receiverSplit);
							} else {
								signal.setReceivers(new String[] { endSplit[endSplit.length-1] });
							}
							
							
						}
						
						identifier.addSignal(signal);
						newLine = reader.readLine();
					}
					
					identifiers.add(identifier);
					line = newLine;
				} else {
					/* not recognized -> get a new line */
					line=null;
				}
				
				if(line==null) {
					line = reader.readLine();
				}
			}
			
			
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Could not open logfile.", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO error while reading logfile.", e);
		}
	}
}
