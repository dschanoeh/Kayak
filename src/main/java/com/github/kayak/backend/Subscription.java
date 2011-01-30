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
 * A Subscription describes the relation between a class that acts as a frame
 * source and a class that wants to receive frames. The receiver can subscribe
 * or unsubscribe IDs and the source can check if a frame should be delivered
 * to the receiver. 
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class Subscription {
	private ArrayList<Integer> ids;
	private Boolean muted;
	private Boolean subscribeAll;
	private FrameReceiver receiver;
	private SubscriptionChangeReceiver changeReceiver;
	
	public Subscription(FrameReceiver receiver, SubscriptionChangeReceiver changeReceiver) {
		ids = new ArrayList<Integer>();
		muted = false;
		subscribeAll = false;
		this.receiver = receiver;
		this.changeReceiver = changeReceiver;
	}
	
	public void subscribe(int id) {
		ids.add(id);
		changeReceiver.subscribed(id, this);
	}
	
	public void subscribeRange(int from, int to) {		
		for(int i=from;i<=to;i++) {
			ids.add(i);
			changeReceiver.subscribed(i, this);
		}
	}
	
	public void unsubscribe(int id) {
		ids.remove(id);
		changeReceiver.unsubscribed(id, this);
	}
	
	public void unsubscribeRange(int from, int to) {
		for(int i=from;i<=to;i++) {
			ids.remove(i);
			changeReceiver.unsubscribed(i, this);
		}
	}
	
	public Boolean isMuted() {
		return muted;
	}

	public void setMuted(Boolean muted) {
		this.muted = muted;
	}
	
	public boolean includes(int id) {
		if(subscribeAll)
			return true;
		else
			if(ids.contains(id))
				return true;
		return false;
	}
	
	public void deliverFrame(Frame frame) {
		if(subscribeAll)
			receiver.newFrame(frame);
		else {
			if(ids.contains(frame.getIdentifier()))
				receiver.newFrame(frame);
		}
	}

	public void setSubscribeAll(Boolean subscribeAll) {
		this.subscribeAll = subscribeAll;
		changeReceiver.subscriptionAllChanged(subscribeAll, this);
	}

	public Boolean getSubscribeAll() {
		return subscribeAll;
	}
}
