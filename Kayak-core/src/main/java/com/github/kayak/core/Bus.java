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

import com.github.kayak.core.description.BusDescription;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A Bus is the virtual representation of a CAN bus. It connects different
 * FrameSources and FrameReceivers for message transport. It also sets the time
 * stamp of each frame to synchronize the message flow.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class Bus implements SubscriptionChangeListener {

    private static final Logger logger = Logger.getLogger(Bus.class.getName());

    private final Set<Subscription> subscriptionsRAW = Collections.synchronizedSet(new HashSet<Subscription>());
    private final Set<Subscription> subscriptionsBCM = Collections.synchronizedSet(new HashSet<Subscription>());
    private TimeSource timeSource;
    private RAWConnection rawConnection;
    private BCMConnection bcmConnection;
    private ControlConnection controlConnection;
    private String name;
    private String alias;
    private BusURL url;
    private final HashSet<BusChangeListener> listeners;
    private final HashSet<EventFrameListener> eventFrameListeners;
    private TimeSource.Mode mode = TimeSource.Mode.STOP;
    private final Set<Integer> subscribedIDs = Collections.synchronizedSet(new HashSet<Integer>());;
    private final HashSet<StatisticsListener> statisticsListeners;
    private BusDescription description;
    private long delta=0; /* delta between socketcand system time and local timesource */

    public static final Pattern BUS_NAME_PATTERN = Pattern.compile("[a-z0-9]{1,16}");

    private StatisticsListener statisticsReceiver = new StatisticsListener() {

        @Override
        public void statisticsUpdated(long rxBytes, long rxPackets, long tBytes, long tPackets) {
            for(StatisticsListener s : statisticsListeners) {
                s.statisticsUpdated(rxBytes, rxPackets, tBytes, tPackets);
            }
        }
    };

    private TimeEventReceiver timeReceiver = new TimeEventReceiver() {

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

            delta = 0;
        }
    };

    @Override
    public String toString() {
        if(name == null) {
            if(alias == null)
                return super.toString();
            else
                return alias + " ()";
        } else {
            if(alias == null)
                return "(" + name + ")";
            else
                return alias + " (" + name + ")";
        }
    }

    public BusURL getConnection() {
        return url;
    }

    /**
     * Returns the name of the bus. This name is used internally and has to
     * match a specific pattern. A human readable name can be set with 'alias
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(BUS_NAME_PATTERN.matcher(name).matches()) {
            this.name = name;
            notifyListenersName();
        }
    }

    /**
     * Returns the human readable name of the bus.
     */
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
        notifyListenersAlias();
    }

    public void registerStatisticsReceiver(StatisticsListener receiver) {
        if(!statisticsListeners.contains(receiver))
            statisticsListeners.add(receiver);
    }

    public void removeStatisticsReceiver(StatisticsListener receiver) {
            statisticsListeners.remove(receiver);
    }

    public void enableStatistics(int interval) {
        if(controlConnection == null && url != null) {
            controlConnection = new ControlConnection(url);
            controlConnection.open();
        } else if(!controlConnection.isConnected()) {
            controlConnection.open();
        }

        controlConnection.setStatisticsReceiver(statisticsReceiver);
        controlConnection.requestStatistics(interval);
    }

    public void disableStatistics() {
        if(controlConnection != null) {
            controlConnection.disableStatistics();
        }
    }

    /**
     * Shall be called if the bus is removed. This will inform all components
     * using the bus that it will not be present any more. Also all connections
     * are terminated.
     */
    public void destroy() {
        disconnect();
        synchronized(listeners) {
            for(BusChangeListener listener : listeners) {
                if(listener != null)
                    listener.destroyed();
            }
        }
    }

    private FrameListener rawReceiver = new FrameListener() {

        @Override
        public void newFrame(Frame f) {
            if(mode == TimeSource.Mode.PLAY) {
                long timestamp = f.getTimestamp();
                if(timestamp != 0) {
                    if(delta == 0) {
                        delta = (timestamp/1000) - timeSource.getTime();
                    }
                    timestamp -= delta*1000;
                    f.setTimestamp(timestamp);
                } else {
                    f.setTimestamp(timeSource.getTime()*1000);
                }
                deliverRAWFrame(f);
            }
        }
    };

    private FrameListener bcmReceiver = new FrameListener() {

        @Override
        public void newFrame(Frame f) {
            if(mode == TimeSource.Mode.PLAY) {
                long timestamp = f.getTimestamp();
                if(timestamp != 0) {
                    if(delta == 0) {
                        delta = (timestamp/1000) - timeSource.getTime();
                    }
                    timestamp -= delta*1000;
                    f.setTimestamp(timestamp);
                } else {
                    f.setTimestamp(timeSource.getTime()*1000);
                }
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
        if(this.timeSource != null)
            this.timeSource.deregister(timeReceiver);

        this.timeSource = timeSource;

        if(timeSource != null)
            timeSource.register(timeReceiver);
    }

    public Bus() {
        listeners = new HashSet<BusChangeListener>();
        statisticsListeners = new HashSet<StatisticsListener>();
        eventFrameListeners = new HashSet<EventFrameListener>();
    }


    @Override
    public void addSubscription(Subscription s) {
        if(s.getSubscribeAll()) {
            synchronized(subscriptionsRAW) {
                subscriptionsRAW.add(s);
            }
            if(mode == TimeSource.Mode.PLAY)
                openRAWConnection();
        } else {
            synchronized(subscriptionsBCM) {
                subscriptionsBCM.add(s);
            }
            if(mode == TimeSource.Mode.PLAY)
                openBCMConnection();
        }
    }

    @Override
    public void subscribed(int id, Subscription s) {
        if (subscriptionsBCM.contains(s)) {
            /* Check if the ID was already subscribed in any subscription */
            synchronized(subscribedIDs) {
                if(!subscribedIDs.contains(id)) {
                    subscribedIDs.add(id);
                    if (bcmConnection != null && bcmConnection.isConnected()) {
                        bcmConnection.subscribeTo(id, 0, 0);
                    } else {
                        logger.log(Level.WARNING, "A BCM subscription was made but no BCM connection is present");
                    }
                }
            }
        } else {
            logger.log(Level.WARNING, "Unregistered subscription tried to subscribe!");
        }
    }

    @Override
    public void unsubscribed(int id, Subscription s) {
        if(subscriptionsBCM.contains(s)) {
            safeUnsubscribe(id);
        } else {
            logger.log(Level.WARNING, "Unregistered subscription tried to unsubscribe!");
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
            synchronized(subscribedIDs) {
                for(Integer identifier : s.getAllIdentifiers()) {
                    subscribedIDs.add(identifier);
                }
            }
        }
    }

    @Override
    public void subscriptionTerminated(Subscription s) {
        removeSubscription(s);
    }

    /**
     * Try to unsubscribe from the identifier. First all other subscriptions
     * are checked if they subscribe to this identifier. If so nothing will
     * be done.
     * @param identifier
     */
    private void safeUnsubscribe(Integer identifier) {
        Boolean found = Boolean.FALSE;
        synchronized(subscriptionsBCM) {
            for (Subscription subscription : subscriptionsBCM) {
                if (subscription.includes(identifier)) {
                    found = Boolean.TRUE;
                    break;
                }
            }
        }
        if (!found) {
            synchronized(subscribedIDs) {
                subscribedIDs.remove(identifier);
            }
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
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    public void removeBusChangeListener(BusChangeListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Connect the Bus to a given BusURL. Internally a RAW- and a BCM
     * connection will be opened. If the Bus was already connected it
     * is disconnected first.
     */
    public void setConnection(BusURL url) {
        disconnect();

        this.url = url;

        if(url != null) {
            rawConnection = new RAWConnection(url);
            bcmConnection = new BCMConnection(url);

            rawConnection.setListener(rawReceiver);
            bcmConnection.setListener(bcmReceiver);
        } else {
            rawConnection = null;
            bcmConnection = null;
    }

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

        notifyListenersConnection();
    }

    /**
     * Send a frame on the bus. All FrameReceivers will also receive this
     * frame.
     */
    public void sendFrame(Frame frame) {
        /* Try to open BCM connection if not present */
        if(url != null) {
            openBCMConnection();

            if (bcmConnection != null) {
                bcmConnection.sendFrame(frame);
            }
        /* If no BCM connection is present we have to do loopback locally */
        } else {
            frame.setTimestamp(timeSource.getTime() * 1000);
            deliverBCMFrame(frame);
            deliverRAWFrame(frame);
        }
    }

    private void deliverBCMFrame(Frame frame) {
        frame.setBus(this);
        for (Subscription s : subscriptionsBCM) {
            if (!s.isMuted()) {
                s.deliverFrame(frame, this);
            }
        }
    }

    private void deliverRAWFrame(Frame frame) {
        frame.setBus(this);
        for (Subscription s : subscriptionsRAW) {
            if (!s.isMuted()) {
                s.deliverFrame(frame, this);
            }
        }
    }

    private void notifyListenersConnection() {
        synchronized(listeners) {
            for(BusChangeListener listener : listeners) {
                if(listener != null)
                    listener.connectionChanged();
            }
        }
    }

    private void notifyListenersName() {
        synchronized(listeners) {
            for(BusChangeListener listener : listeners) {
                if(listener != null)
                    listener.nameChanged(name);
            }
        }
    }

    private void notifyListenersAlias() {
        synchronized(listeners) {
            for(BusChangeListener listener : listeners) {
                if(listener != null)
                    listener.aliasChanged(alias);
            }
        }
    }

    private void notifyListenersDescriptionChanged() {
        synchronized(listeners) {
            for(BusChangeListener listener : listeners) {
                if(listener != null)
                    listener.descriptionChanged();
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
                logger.log(Level.INFO, "Creating new BCM connection");
                bcmConnection = new BCMConnection(url);
                bcmConnection.setListener(bcmReceiver);
            } else {
                logger.log(Level.WARNING, "Could not open BCM connection because no url was set");
                return;
            }
        }

        if (bcmConnection.isConnected()) {
            return;
        } else {
            logger.log(Level.INFO, "Opening BCM connection and resubscribing all IDs");
            bcmConnection.open();

            /* Check for all present BCM subscriptions and bring the connection
             * up to date.
             */
            synchronized(subscribedIDs) {
                for (Integer identifier : subscribedIDs) {
                    bcmConnection.subscribeTo(identifier, 0, 0);
                }
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
                logger.log(Level.INFO, "Creating new RAW connection");
                rawConnection = new RAWConnection(url);
                rawConnection.setListener(rawReceiver);
            } else {
                logger.log(Level.WARNING, "Could not open RAW connection because no url was set");
                return;
            }
        }

        /* Nothing to do here */
        if(rawConnection.isConnected()) {
            return;
        } else {
            logger.log(Level.INFO, "Opening RAW connection");
            rawConnection.open();
        }
    }

    public void addEventFrameListener(EventFrameListener listener) {
        eventFrameListeners.add(listener);
    }

    public void removeEventFrameListener(EventFrameListener listener) {
        eventFrameListeners.remove(listener);
    }

    public void sendEventFrame(EventFrame f) {
        f.setTimestamp(timeSource.getTime());
        f.setBus(this);

        for(EventFrameListener receiver : eventFrameListeners) {
            if(receiver != null)
                receiver.newEventFrame(f);
        }

        logger.log(Level.INFO, "received event frame{0}", f.getMessage());
    }

    public void setDescription(BusDescription desc) {
        this.description = desc;
        notifyListenersDescriptionChanged();
    }

    public BusDescription getDescription() {
        return description;
    }

}
