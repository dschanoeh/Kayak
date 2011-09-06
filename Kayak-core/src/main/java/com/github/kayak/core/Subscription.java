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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Subscription describes the relation between a class that acts as a frame
 * source and a class that wants to receive frames. The receiver can subscribe
 * or unsubscribe IDs and the source can check if a frame should be delivered
 * to the receiver.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class Subscription {

    private static final Logger logger = Logger.getLogger(Subscription.class.getName());

    private Set<Integer> ids = Collections.synchronizedSet(new HashSet<Integer>());
    private Boolean muted;
    private Boolean subscribeAll;
    private FrameReceiver receiver;
    private SubscriptionChangeReceiver changeReceiver;

    private void logStatus() {
        if(!subscribeAll)
            logger.log(Level.INFO, "[{0} | {1}] Subscribed IDs: {2}", new String[]{receiver.toString(), changeReceiver.toString(), ids.toString()});
        else
            logger.log(Level.INFO, "[{0} | {1}] Subscribed IDs: ALL", new String[]{receiver.toString(), changeReceiver.toString()});


    }

    /**
     * Creates a new Subscription. The new Subscription is automatically
     * registered at the {@link SubscriptionChangeReceiver}.
     * @param receiver
     * @param changeReceiver
     */
    public Subscription(FrameReceiver receiver, SubscriptionChangeReceiver changeReceiver) {
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
        synchronized(this) {
            if (!ids.contains(id)) {
                ids.add(id);
                changeReceiver.subscribed(id, this);
                logStatus();
            }
        }
    }

    /**
     * Subscribe for a range of identifiers. Note that it is not recommended
     * to do this for large ranges because it will need much time.
     * @param from
     * @param to
     */
    public void subscribeRange(int from, int to) {
        synchronized(this) {
            for (int i = from; i <= to; i++) {
                if(!ids.contains(i)) {
                    ids.add(i);
                    changeReceiver.subscribed(i, this);
                }
            }
            logStatus();
        }
    }

    /**
     * Remove all identifiers from the subscription.
     */
    public void clear() {
        Integer[] identifiers = new Integer[0];
        synchronized(this) {
            identifiers = ids.toArray(new Integer[ids.size()]);
        }
        for (int i=0;i<identifiers.length;i++) {
            unsubscribe(identifiers[i]);
        }
        logStatus();
    }

    /**
     * Remove a single identifier from the subscription.
     * @param id
     */
    public void unsubscribe(int id) {
        synchronized(this) {
            if(ids.contains(id)) {
                ids.remove(id);
                changeReceiver.unsubscribed(id, this);
                logStatus();
            }
        }
    }

    /**
     * Remove a range of identifiers. Note that it is not recommended
     * to do this for large ranges because it will need much time.
     * @param from
     * @param to
     */
    public void unsubscribeRange(int from, int to) {
        synchronized(this) {
            for (int i = from; i <= to; i++) {
                if(ids.contains(i)) {
                    ids.remove(i);
                    changeReceiver.unsubscribed(i, this);
                }
            }
        }
        logStatus();
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

    public void deliverFrame(Frame frame, Bus bus) {
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
        logStatus();
    }

    /**
     * Returns true if all IDs are subscribed by this Subscription
     */
    public Boolean getSubscribeAll() {
        return subscribeAll;
    }

    public Set<Integer> getAllIdentifiers() {
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Informs the {@link SubscriptionChangeReceiver} that the Subscription
     * will not be used anymore and no more Frames shall be received.
     */
    public void Terminate() {
        if(changeReceiver != null)
            changeReceiver.subscriptionTerminated(this);
    }
}
