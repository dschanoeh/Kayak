/**
 * 	This file is part of Kayak.
 *	
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *	
 */
package com.github.kayak.snapshot;

import java.util.ArrayList;
import java.util.Collections;

import com.github.kayak.backend.*;

/**
 * A SnapshotBuffer can be connected to a {@link Bus} and then buffers
 * all {@link Frame}s that are on the bus. Therefore it adds a RAW
 * subscription. After receiving Frames the Buffer can be connected to
 * a different (or the same) bus to replay the frames.
 * The depth of the buffer may be set and is per default 5 seconds.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class SnapshotBuffer {
	private int depth = 5000;
	private Bus bus;
	private Subscription subscription;
	private ArrayList<Frame> frames;
	private Thread cleanupThread;
	private Thread replayThread;
	private Boolean stopRequest = false;
	private Boolean paused = false;
	private TimeSource timeSource;
	
	private FrameReceiver receiver = new FrameReceiver() {
		@Override
		public void newFrame(Frame frame) {
			synchronized(frames) {
				frames.add(frame);
			}
		}
	};
	
	private Runnable cleanupRunnable = new Runnable() {
		@Override
		public void run() {
			while(!stopRequest) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					if(stopRequest)
						return;
				}
				long currentTime = bus.getTimeSource().getTime();
				synchronized(frames) {
					for(Frame f : frames) {
						if(f.getTimestamp() < currentTime-depth) {
							frames.remove(f);
						}
					}
				}
			}
		}
	};
	
	private Runnable replayRunnable = new Runnable() {
		@Override
		public void run() {
			/* first frame will be sent immediately and taken as a
			 * reference
			 */
			long ref = frames.get(0).getTimestamp();
			for(Frame f : frames) {
				/* if playback is paused we simply wait
				 * for the replay to be continued
				 */
				if(paused) {
					while(paused)
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							if(stopRequest)
								return;
						}
				}
				
				long difference = (f.getTimestamp() - ref) - timeSource.getTime();
				
				if(difference > 5) {
					try {
						Thread.sleep(difference);
					} catch (InterruptedException e) {}
				}
				
				bus.sendFrame(f);
				
				if(stopRequest)
					break;
			}
			
		}
	};
	
	private TimeEventReceiver timeEventReceiver = new TimeEventReceiver() {

		@Override
		public void paused() {
			paused = true;
		}

		@Override
		public void played() {	
			stopRequest = false;
			paused = false;
			if(replayThread == null) {
				replayThread = new Thread(replayRunnable);
				replayThread.start();
			} 
		}

		@Override
		public void stopped() {
			stopRequest = true;
			try {
				replayThread.interrupt();
				replayThread.join();
			} catch (InterruptedException e) {}
			
			replayThread = null;
		}
		
	};

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		if(depth > 0)
			this.depth = depth;
	}
	
	public SnapshotBuffer() {
		frames = new ArrayList<Frame>();
		frames.ensureCapacity(500);
	}
	
	/**
	 * Connect the buffer to a bus to receive frames. This is done through
	 * a RAW subscription that is added to the bus.
	 * @param bus The bus to receive Frames from
	 */
	public void connectForBuffering(Bus bus) {
		disconnectFromReplay();
		
		this.bus = bus;
		frames.clear();
		subscription = new Subscription(receiver, bus);
		subscription.setSubscribeAll(true);
		bus.addRAWSubscription(subscription);
		
		stopRequest = false;
		cleanupThread = new Thread(cleanupRunnable);
		cleanupThread.start();
	}
	
	/**
	 * Stop buffering from the bus. No more frames will be added. The
	 * connection to the bus is terminated.
	 */
	public void disconnectFromBuffering() {
		stopRequest = true;
		
		if(cleanupThread != null && cleanupThread.isAlive()) {
			try {
				replayThread.interrupt();
				cleanupThread.join();
			} catch (InterruptedException e) {}
		}
		
		if(bus != null) {
			bus.removeRAWSubscription(subscription);
			bus = null;
		}
	}

	/**
	 * Connect a bus for replay.
	 * @param bus Bus to which the frames shall be played
	 * @param source TimeSource to control the replay
	 */
	public void connectForReplay(Bus bus, TimeSource source) {
		/* make sure no more frames are added */
		disconnectFromBuffering();
		
		this.bus = bus;
		this.timeSource = source;
		timeSource.register(timeEventReceiver);
		
		/* no need to start replay. The TimeSource will give the
		 * start signal and the timeEventReceiver will then start
		 * the thread. 
		 */
	}
	
	/**
	 * Stop any replay activity and make the buffer free again for frame
	 * reception.
	 */
	public void disconnectFromReplay() {
		stopRequest = true;
		
		if(timeSource != null)
			timeSource.deregister(timeEventReceiver);
		
		if(replayThread != null && replayThread.isAlive()) {
			try {
				replayThread.interrupt();
				replayThread.join();
			} catch (InterruptedException e) {}
		}
		
		if(bus != null) {
			bus = null;
		}
	}
}
