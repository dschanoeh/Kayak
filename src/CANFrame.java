import java.util.Date;


public class CANFrame {
	byte[] data;
	int identifier;
	Date timestamp;
	
	public CANFrame() {
		
	}
	
	public CANFrame(int identifier, byte[] data) {
		this.identifier = identifier;
		this.data = data;
	}
	
	public CANFrame(int identifier, byte[] data, Date timestamp) {
		this.identifier = identifier;
		this.data = data;
		this.timestamp = timestamp;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public int getLength() {
		return data.length;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
}
