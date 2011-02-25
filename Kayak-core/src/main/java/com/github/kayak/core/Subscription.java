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
package com.github.kayak.core;

import java.util.HashSet;
import java.util.Set;

/**
 * A Subscription describes the relation between a class that acts as a frame
 * source and a class that wants to receive frames. The receiver can subscribe
 * or unsubscribe IDs and the source can check if a frame should be delivered
 * to the receiver. 
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class Subscription {

    private HashSet<Integer> ids;
    private Boolean muted;
    private Boolean subscribeAll;
    private FrameReceiver receiver;
    private SubscriptionChangeReceiver changeReceiver;

    /**
     * Creates a new Subscription. The new Subscription is automatically
     * registered at the {@link SubscriptionChangeReceiver}.
     * @param receiver
     * @param changeReceiver
     */
    public Subscription(FrameReceiver receiver, SubscriptionChangeReceiver changeReceiver) {
        ids = new HashSet<Integer>();
        muted = false;
        subscribeAll = false;
        this.receiver = receiver;
        this.changeReceiver = changeReceiver;
        
        changeReceiver.addSubscription(this);
    }

    /**
     * Subscribe for a single identifier
     * @param id identifier
     */
    public void subscribe(int id) {
        if (!ids.contains(id)) {
            ids.add(id);
            changeReceiver.subscribed(id, this);
        }
    }

    /**
     * Subscribe for a range of identifiers. Note that it is not recommended
     * to do this for large ranges because it will need much time.
     * @param from
     * @param to
     */
    public void subscribeRange(int from, int to) {
        for (int i = from; i <= to; i++) {
            if(!ids.contains(i)) {
                ids.add(i);
                changeReceiver.subscribed(i, this);
            }
        }
    }

    /**
     * Remove all identifiers from the subscription.
     */
    public void clear() {
        for (Integer id : ids) {
            unsubscribe(id);
        }
    }

    /**
     * Remove a single identifier from the subscription.
     * @param id
     */
    public void unsubscribe(int id) {
        if(ids.contains(id)) {
            ids.remove(id);
            changeReceiver.unsubscribed(id, this);
        }
    }

    /**
     * Remove a range of identifiers. Note that it is not recommended
     * to do this for large ranges because it will need much time.
     * @param from
     * @param to
     */
    public void unsubscribeRange(int from, int to) {
        for (int i = from; i <= to; i++) {
            if(ids.contains(i)) {
                ids.remove(i);
                changeReceiver.unsubscribed(i, this);
            }
        }
    }

    public Boolean isMuted() {
        return muted;
    }

    public void setMuted(Boolean muted) {
        this.muted = muted;
    }

    public boolean includes(int id) {
        if (subscribeAll) {
            return true;
        } else if (ids.contains(id)) {
            return true;
        }
        return false;
    }

    public void deliverFrame(Frame frame) {
        if (subscribeAll) {
            receiver.newFrame(frame);
        } else {
            if (ids.contains(frame.getIdentifier())) {
                receiver.newFrame(frame);
            }
        }
    }

    public void setSubscribeAll(Boolean subscribeAll) {
        this.subscribeAll = subscribeAll;
        changeReceiver.subscriptionAllChanged(subscribeAll, this);
    }

    public Boolean getSubscribeAll() {
        return subscribeAll;
    }

    public Set<Integer> getAllIdentifiers() {
        return ids;
    }

    public void Terminate() {
        if(changeReceiver != null)
            changeReceiver.subscriptionTerminated(this);
    }
}
