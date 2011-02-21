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
    private RAWConnection rawConnection;
    private BCMConnection bcmConnection;
    private String name;
    private BusURL connection;
    private ArrayList<BusChangeListener> listeners;

    public BusURL getConnection() {
        return connection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private FrameReceiver rawReceiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame f) {
            deliverRAWFrame(f);
        }
    };

    private FrameReceiver bcmReceiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame f) {
            deliverBCMFrame(f);
        }
    };

    public TimeSource getTimeSource() {
        return timeSource;
    }

    /**
     * Set the {@link TimeSource} that will be used to coordinate
     * the message flow on the bus (play, pause, timestamps...)
     */
    public void setTimeSource(TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    public Bus() {
        subscriptionsRAW = new ArrayList<Subscription>();
        subscriptionsBCM = new ArrayList<Subscription>();
        listeners = new ArrayList<BusChangeListener>();
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

    /**
     * A BusChangeListener may use this method if he wants to be notified
     * about changes to the bus like added connections etc.
     */
    public void addBusChangeListener(BusChangeListener listener) {
        listeners.add(listener);
    }

    public void removeBusChangeListener(BusChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Connect the Bus to a given BusURL. Internally a RAW- and a BCM
     * connection will be opened. If the Bus was already connected it
     * is disconnected first.
     */
    public void setConnection(BusURL url) {
        disconnect();
        
        this.connection = url;
        /* FIXME we won't do this now
        rawConnection = new RAWConnection(url);
        bcmConnection = new BCMConnection(url);*/

        notifyListenersConnection();
    }

    /**
     * If the Bus was connected terminate all connections.
     */
    public void disconnect() {
        /* FIXME we won't do this now
        if (rawConnection != null) {
            rawConnection.close();
        }

        if (bcmConnection != null) {
            bcmConnection.close();
        }*/

        connection = null;

        notifyListenersConnection();
    }

    /**
     * Send a frame on the bus. All FrameReceivers will also receive this
     * frame.
     */
    public void sendFrame(Frame frame) {
        if (bcmConnection != null) {
            bcmConnection.sendFrame(frame);
            /* If no BCM connection is present we have to do loopback locally */
        } else {
            deliverBCMFrame(frame);
            deliverRAWFrame(frame);
        }
    }

    private void deliverBCMFrame(Frame frame) {
        for (Subscription s : subscriptionsBCM) {
            if (!s.isMuted()) {
                s.deliverFrame(frame);
            }
        }
    }

    private void deliverRAWFrame(Frame frame) {
        for (Subscription s : subscriptionsRAW) {
            if (!s.isMuted()) {
                s.deliverFrame(frame);
            }
        }
    }

    private void notifyListenersConnection() {
        for(BusChangeListener listener : listeners) {
            listener.connectionChanged();
        }
    }

    @Override
    public void subscribed(int id, Subscription s) {
        if (subscriptionsBCM.contains(s)) {
            if (bcmConnection != null) {
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
        for (Subscription sub : subscriptionsBCM) {
            if (sub.includes(id)) {
                found = true;
                break;
            }
        }

        if (!found) {
            if (bcmConnection != null) {
                bcmConnection.unsubscribeFrom(id);
            } else {
                logger.log(Level.WARNING, "A BCM unsubscription was made but no BCM connection is present");
            }
        }
    }

    @Override
    public void subscriptionAllChanged(boolean all, Subscription s) {
        if (all == true) {
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
