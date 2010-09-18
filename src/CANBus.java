import java.util.ArrayList;


public abstract class CANBus {
	ArrayList<FrameReceiver> receivers;
	
	public CANBus() {
		receivers = new ArrayList<FrameReceiver>();
	}
	
	public void registerReceiver(FrameReceiver receiver) {
		if(!receivers.contains(receiver)) {
			receivers.add(receiver);
		}
	}
	
	public void deregisterReceiver(FrameReceiver receiver) {
		receivers.remove(receiver);
	}
	
	private void sendFrame(CANFrame frame) {
		for(FrameReceiver r : receivers) {
			r.newFrame(frame);
		}
	}
	
}
