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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Bus is the virtual representation of a CAN bus. It connects different
 * FrameSources and FrameReceivers for message transport. It also sets the time
 * stamp of each frame to synchronize the message flow.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class Bus implements SubscriptionChangeReceiver, TimeEventReceiver {

    private static final Logger logger = Logger.getLogger(Bus.class.getName());
    private ArrayList<Subscription> subscriptionsRAW;
    private ArrayList<Subscription> subscriptionsBCM;
    private TimeSource timeSource;
    private RAWConnection rawConnection;
    private BCMConnection bcmConnection;
    private String name;
    private BusURL url;
    private ArrayList<BusChangeListener> listeners;
    private TimeSource.Mode mode = TimeSource.Mode.STOP;
    private HashSet<Integer> subscribedIDs;

    public BusURL getConnection() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyListenersName();
    }

    private FrameReceiver rawReceiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame f) {
            if(mode == TimeSource.Mode.PLAY) {
                f.setTimestamp(timeSource.getTime());
                deliverRAWFrame(f);
            }
        }
    };

    private FrameReceiver bcmReceiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame f) {
            if(mode == TimeSource.Mode.PLAY) {
                f.setTimestamp(timeSource.getTime());
                deliverBCMFrame(f);
            }
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
        subscribedIDs = new HashSet<Integer>();
    }

    @Override
    public void addSubscription(Subscription s) {
        if(s.getSubscribeAll()) {
            subscriptionsRAW.add(s);
            if(mode == TimeSource.Mode.PLAY)
                openRAWConnection();
        } else {
            subscriptionsBCM.add(s);
            if(mode == TimeSource.Mode.PLAY)
                openBCMConnection();
        }
    }

    /**
     * Try to unsubscribe from the identifier. First all other subscriptions
     * are checked if they subscribe to this identifier. If so nothing will
     * be done.
     * @param identifier
     */
    private void safeUnsubscribe(Integer identifier) {
        Boolean found = false;
        for (Subscription subscription : subscriptionsBCM) {
            if (subscription.includes(identifier)) {
                found = true;
                break;
            }
        }
        if (!found) {
            subscribedIDs.remove(identifier);
            if(bcmConnection != null && bcmConnection.isConnected()) {
                bcmConnection.unsubscribeFrom(identifier);
            }
        }
    }

    /**
     * Remove a subscription from the list of subscriptions. If it is possible
     * to unsubscribe identifiers or even close connections this is done.
     * @param s
     */
    private void removeSubscription(Subscription s) {

        if(subscriptionsRAW.contains(s)) {
            subscriptionsRAW.remove(s);

            /* was this the last RAW subscription? */
            if(subscriptionsRAW.isEmpty() && rawConnection != null && rawConnection.isConnected())
                rawConnection.close();
        }

        if(subscriptionsBCM.contains(s)) {
            subscriptionsBCM.remove(s);

            Set<Integer> identifiers = s.getAllIdentifiers();
            for(Integer identifier : identifiers) {
                safeUnsubscribe(identifier);
            }
        }
    }

    /**
     * A {@link BusChangeListener} may use this method if he wants to be notified
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
        
        this.url = url;

        rawConnection = new RAWConnection(url);
        bcmConnection = new BCMConnection(url);

        rawConnection.setReceiver(rawReceiver);
        bcmConnection.setReceiver(bcmReceiver);

        notifyListenersConnection();
    }

    /**
     * If the Bus was connected terminate all connections.
     */
    public void disconnect() {
        if (rawConnection != null && rawConnection.isConnected()) {
            rawConnection.close();
        }

        if (bcmConnection != null && bcmConnection.isConnected()) {
            bcmConnection.close();
        }

        url = null;

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

    private void notifyListenersName() {
        for(BusChangeListener listener : listeners) {
            listener.nameChanged();
        }
    }

    @Override
    public void subscribed(int id, Subscription s) {
        if (subscriptionsBCM.contains(s)) {
            /* Check if the ID was already subscribed in any subscription */
            if(!subscribedIDs.contains(id)) {
                subscribedIDs.add(id);
                if (bcmConnection != null && bcmConnection.isConnected()) {
                    bcmConnection.subscribeTo(id, 0, 0);
                } else {
                    logger.log(Level.WARNING, "A BCM subscription was made but no BCM connection is present");
                }
            }
        }
    }

    @Override
    public void unsubscribed(int id, Subscription s) {
        if(subscriptionsBCM.contains(s)) {
            safeUnsubscribe(id);
        }
    }

    @Override
    public void subscriptionAllChanged(boolean all, Subscription s) {
        /* BCM subscription switched to RAW subscription */
        if (all == true) {
            subscriptionsBCM.remove(s);
            if(!subscriptionsRAW.contains(s))
                subscriptionsRAW.add(s);

            Set<Integer> ids = s.getAllIdentifiers();
            for(Integer identifier : ids) {
                safeUnsubscribe(identifier);
            }

            if(mode == TimeSource.Mode.PLAY) {
                openRAWConnection();
            }
        /* RAW subscription switched to BCM subscription */
        } else {
            subscriptionsRAW.remove(s);
            if(!subscriptionsBCM.contains(s))
                subscriptionsBCM.add(s);

            if(subscriptionsRAW.isEmpty() && rawConnection != null && rawConnection.isConnected()) {
                logger.log(Level.INFO, "No more raw subscriptions. Closing connection.");
                rawConnection.close();
            }

            /* Make sure BCM connection is opened */
            if(mode == TimeSource.Mode.PLAY) {
                openBCMConnection();
                
                for(Integer identifier : s.getAllIdentifiers()) {
                    bcmConnection.subscribeTo(identifier, 0, 0);
                }
            }
            
            for(Integer identifier : s.getAllIdentifiers()) {
                subscribedIDs.add(identifier);
            }
        }
    }

    /**
     * Checks if the BCM connection exists and is connected. If not tries
     * to create a new one and/or connects it. If there are subscriptions
     * to identifiers they will be subscribed in the connection.
     */
    private void openBCMConnection() {
        /* If the connection was not created yet try to create connection */
        if(bcmConnection == null) {
            if(url != null) {
                bcmConnection = new BCMConnection(url);
                bcmConnection.setReceiver(bcmReceiver);
            } else {
                return;
            }
        } 
       
        if (bcmConnection.isConnected()) {
            return;
        } else {
            bcmConnection.open();

            /* Check for all present BCM subscriptions and bring the connection
             * up to date.
             */
            for (Integer identifier : subscribedIDs) {
                bcmConnection.subscribeTo(identifier, 0, 0);
            }
        }

    }

    /**
     * Checks if the raw connection exists and is connected. If not tries
     * to create a new one and/or connects it.
     */
    private void openRAWConnection() {
        /* If the connection was not created yet try to create connection */
        if(rawConnection == null) {
            if(url != null) {
                rawConnection = new RAWConnection(url);
                rawConnection.setReceiver(rawReceiver);
            } else {
                return;
            }
        }

        /* Nothing to do here */
        if(rawConnection.isConnected()) {
            return;
        } else {
            rawConnection.open();
        }
    }

    @Override
    public void subscriptionTerminated(Subscription s) {
        removeSubscription(s);
    }

    @Override
    public void paused() {
        mode = TimeSource.Mode.PAUSE;
    }

    /**
     * Time is started. Connections are opened if they are required
     */
    @Override
    public void played() {
        mode = TimeSource.Mode.PLAY;

        if (!subscriptionsBCM.isEmpty()) {
            openBCMConnection();
        }

        if (!subscriptionsRAW.isEmpty()) {
            openRAWConnection();
        }
    }

    @Override
    public void stopped() {
        mode = TimeSource.Mode.STOP;

        if(bcmConnection != null && bcmConnection.isConnected())
            bcmConnection.close();

        if(rawConnection != null && rawConnection.isConnected())
            rawConnection.close();
    }
}
