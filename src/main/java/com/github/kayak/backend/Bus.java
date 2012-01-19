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
package com.github.kayak.backend;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Bus is the virtual representation of a CAN bus. It connects different
 * FrameSources and FrameReceivers for message transport. It also sets the time
 * stamp of each frame to synchronize the message flow.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class Bus implements SubscriptionChangeReceiver {
	private Logger logger = Logger.getLogger("com.github.kayak.backend.bus");
	private ArrayList<Subscription> subscriptionsRAW;
	private ArrayList<Subscription> subscriptionsBCM;
	private TimeSource timeSource;
	private com.github.kayak.canio.kcd.Bus busDefinition;
	private RAWConnection rawConnection;
	private BCMConnection bcmConnection;
	
	private FrameReceiver rawReceiver = new FrameReceiver() {
		public void newFrame(Frame f) {
			deliverRAWFrame(f);
		}
	};
	
	private FrameReceiver bcmReceiver = new FrameReceiver() {
		public void newFrame(Frame f) {
			deliverBCMFrame(f);
		}
	};
	
	public String getName() {
		if(busDefinition != null)
			return busDefinition.getName();
		return null;
	}

	public TimeSource getTimeSource() {
		return timeSource;
	}

	public void setTimeSource(TimeSource timeSource) {
		this.timeSource = timeSource;
	}

	public Bus() {
		subscriptionsRAW = new ArrayList<Subscription>();
		subscriptionsBCM = new ArrayList<Subscription>();
		
	}
	
	public Bus(com.github.kayak.canio.kcd.Bus busDefinition) {
		subscriptionsRAW = new ArrayList<Subscription>();
		subscriptionsBCM = new ArrayList<Subscription>();
		this.busDefinition = busDefinition;
	}
	
	public void addRAWSubscription(Subscription s) {
		subscriptionsRAW.add(s);
	}
	
	public void addBCMSubscription(Subscription s) {
		subscriptionsBCM.add(s);
	}
	
	public void removeRAWSubscription(Subscription s) {
		subscriptionsRAW.remove(s);
	}
	
	public void removeBCMSubscription(Subscription s) {
		subscriptionsBCM.remove(s);
	}
	
	public void connectTo(RAWConnection conn) {
		this.rawConnection = conn;
		conn.setReceiver(rawReceiver);
	}
	
	public void connectTo(BCMConnection conn) {
		this.bcmConnection = conn;
		conn.setReceiver(bcmReceiver);
	}
	
	public void disconnect() {
		if(rawConnection != null)
			rawConnection.setReceiver(null);
		
		if(bcmConnection != null)
			bcmConnection.setReceiver(null);
	}
	
	public void sendFrame(Frame frame) {
		if(bcmConnection != null) {
			bcmConnection.sendFrame(frame);
		/* If no BCM connection is present we have to do loopback locally */
		} else {
			deliverBCMFrame(frame);
			deliverRAWFrame(frame);
		}
	}
	
	private void deliverBCMFrame(Frame frame) {
		for(Subscription s : subscriptionsBCM) {
			if(!s.isMuted())
				s.deliverFrame(frame);
		}
	}
	
	private void deliverRAWFrame(Frame frame) {
		for(Subscription s : subscriptionsRAW) {
			if(!s.isMuted())
				s.deliverFrame(frame);
		}
	}

	@Override
	public void subscribed(int id, Subscription s) {
		if(subscriptionsBCM.contains(s)) {
			if(bcmConnection != null) {
				bcmConnection.subscribeTo(id, 0, 0);
			} else {
				logger.log(Level.WARNING, "A BCM subscription was made but no BCM connection is present");
			}
		}
	}

	@Override
	public void unsubscribed(int id, Subscription s) {
		/* only if every other subscription does not include the id
		 * the BCM connection can be told to fully unsubscribe the id.
		 * otherwise the deliverBCMFrame method will handle filtering.
		 */
		Boolean found = false;
		for(Subscription sub : subscriptionsBCM) {
			if(sub.includes(id)) {
				found = true;
				break;
			}
		}
		
		if(!found) {
			if(bcmConnection != null) {
				bcmConnection.unsubscribeFrom(id);
			} else {
				logger.log(Level.WARNING, "A BCM unsubscription was made but no BCM connection is present");
			}
		}	
	}

	@Override
	public void subscriptionAllChanged(boolean all, Subscription s) {
		if(all == true) {
			subscriptionsBCM.remove(s);
			subscriptionsRAW.add(s);
			
			/* TODO unsubscribe from all ids of the subscription */
		} else {
			subscriptionsRAW.remove(s);
			subscriptionsBCM.add(s);
			
			/* TODO resubscribe to all ids of the subscription */
		}
		
	}
}
