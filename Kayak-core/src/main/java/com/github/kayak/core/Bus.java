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
public class Bus implements SubscriptionChangeReceiver {

    private static final Logger logger = Logger.getLogger("com.github.kayak.backend.bus");
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
        notifyListenersName();
    }

    private FrameReceiver rawReceiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame f) {
            f.setTimestamp(timeSource.getTime());
            deliverRAWFrame(f);
        }
    };

    private FrameReceiver bcmReceiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame f) {
            f.setTimestamp(timeSource.getTime());
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

    @Override
    public void addSubscription(Subscription s) {
        if(s.getSubscribeAll()) {
            subscriptionsRAW.add(s);
            openRAWConnection();
        } else {
            subscriptionsBCM.add(s);
            openBCMConnection();
        }
    }

    private void removeSubscription(Subscription s) {
        subscriptionsRAW.remove(s);
        subscriptionsBCM.remove(s);

        if(subscriptionsBCM.isEmpty() && bcmConnection != null && bcmConnection.isConnected())
            bcmConnection.close();

        if(subscriptionsRAW.isEmpty() && rawConnection != null && rawConnection.isConnected())
            rawConnection.close();
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
        /* FIXME we won't do this now*/
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
        /* FIXME we won't do this now*/
        if (rawConnection != null && rawConnection.isConnected()) {
            rawConnection.close();
        }

        if (bcmConnection != null && bcmConnection.isConnected()) {
            bcmConnection.close();
        }

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

    private void notifyListenersName() {
        for(BusChangeListener listener : listeners) {
            listener.nameChanged();
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
            if(!subscriptionsRAW.contains(s))
                subscriptionsRAW.add(s);

            /* If no one needs the BCM connection close it */
            if(subscriptionsBCM.isEmpty() && bcmConnection != null && bcmConnection.isConnected()) {
                logger.log(Level.INFO, "No more BCM subscriptions. Closing connection.");
                bcmConnection.close();
            /* Otherwise check if we can unsubscribe from the used identifiers */
            } else {
                Set<Integer> ids = s.getAllIdentifiers();
                for(Integer identifier : ids) {
                    Boolean found = false;

                    for(Subscription subscription : subscriptionsBCM) {
                        if(subscription.includes(identifier)) {
                            found = true;
                            return;
                        }
                    }

                    if(!found)
                        bcmConnection.unsubscribeFrom(identifier);
                }
            }

            openRAWConnection();
        } else {
            subscriptionsRAW.remove(s);
            if(!subscriptionsBCM.contains(s))
                subscriptionsBCM.add(s);

            if(subscriptionsRAW.isEmpty() && rawConnection != null && rawConnection.isConnected()) {
                logger.log(Level.INFO, "No more raw subscriptions. Closing connection.");
                rawConnection.close();
            }

            /* Make sure BCM connection is opened */
            openBCMConnection();

            /* Make sure we get all identifiers that are in the subscription */
            Set<Integer> identifiers = s.getAllIdentifiers();
            for(Integer identifier : identifiers) {
                bcmConnection.subscribeTo(identifier, 0, 0);
            }
        }

    }

    /**
     * Checks if the BCM connection exists and is connected. If not tries
     * to create a new one and/or connects it.
     */
    private void openBCMConnection() {
        /* If the connection was not created yet try to create connection */
        if(bcmConnection == null) {
            if(connection != null) {
                bcmConnection = new BCMConnection(connection);
            } else {
                return;
            }
        }

        /* Nothing to do here */
        if(bcmConnection.isConnected())
            return;

        bcmConnection.setReceiver(bcmReceiver);
        bcmConnection.open();

        /* Check for all present BCM subscriptions and bring the connection
         * up to date.
         */
        for(Subscription s : subscriptionsBCM) {
            /* TODO implement */
        }
    }

    /**
     * Checks if the raw connection exists and is connected. If not tries
     * to create a new one and/or connects it.
     */
    private void openRAWConnection() {
        /* If the connection was not created yet try to create connection */
        if(rawConnection == null) {
            if(connection != null) {
                rawConnection = new RAWConnection(connection);
            } else {
                return;
            }
        }

        /* Nothing to do here */
        if(rawConnection.isConnected())
            return;

        rawConnection.setReceiver(rawReceiver);
        rawConnection.open();
    }

    @Override
    public void subscriptionTerminated(Subscription s) {
        removeSubscription(s);
    }
}
