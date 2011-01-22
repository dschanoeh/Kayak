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

/**
 * A Bus is the virtual representation of a CAN bus. It connects different
 * FrameSources and FrameReceivers for message transport. It also sets the timestamp
 * of each frame to synchronize the message flow.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class Bus implements SubscriptionChangeReceiver {
	private ArrayList<Subscription> subscriptionsRAW;
	private ArrayList<Subscription> subscriptionsBCM;
	private TimeSource timeSource;
	private com.github.kayak.canio.kcd.Bus busDefinition;
	
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
	
	private void deliverBCMFrame(Frame frame) {
		for(Subscription s : subscriptionsBCM) {
			if(!s.isMuted())
				s.deliverFrame(frame);
		}
	}
	
	private void deliverRAWFrame(Frame frame) {
		for(Subscription s : subscriptionsBCM) {
			if(!s.isMuted())
				s.deliverFrame(frame);
		}
	}

	@Override
	public void subscribed(int id, Subscription s) {
		/* TODO subscribe to id */
	}

	@Override
	public void unsubscribed(int id, Subscription s) {
		/* TODO :) check all other subscriptions if a
		 * BCM subscription can be removed
		 */
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
