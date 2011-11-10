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

import java.util.concurrent.locks.LockSupport;

/**
 * A send job sends a single CAN frame at a given interval time. If the bus
 * is connected to a remote socketcand the send job is fowarded to SocketCAN
 * for higher timestamp precision.
 * If there is no connection the sending is done locally.
 * Each property change does immediately update the send job.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class SendJob {

    private int id;
    private byte[] data;
    private long interval;
    private Thread thread;
    private Bus bus;
    private boolean local = false;
    private boolean sending = false;
    private boolean extended = false;

    public byte[] getData() {
        return data;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;

        if(sending && !local) {
             stopRemoteSending();
             startRemoteSending();
        }
    }

    public void setData(byte[] data) {
        this.data = data;

        if(sending && !local) {
             stopRemoteSending();
             startRemoteSending();
        }
    }

    public int getId() {
        return id;
    }

    public boolean isSending() {
        return sending;
    }

    public void setId(int id) {
        int oldId = this.id;
        this.id = id;

        if(sending && !local) {
            /* remove old send job */
            bus.removeSendJob(oldId, extended);

            startRemoteSending();
        }
    }

    /**
     * Get the interval in microseconds
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Set the interval in microseconds
     * @param interval
     */
    public void setInterval(long interval) {
        this.interval = interval;

        if(sending && !local) {
            /* remove old send job */
            bus.removeSendJob(id, extended);

            startRemoteSending();
        }
    }

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            while(true) {
                Frame f = new Frame(id, extended, data);

                bus.sendFrame(f);
                if(Thread.interrupted())
                    return;
                LockSupport.parkNanos(interval*1000);
            }
        }
    };

    public SendJob(int id, boolean extended, byte[] data, long usec) {
        this.id = id;
        this.data = data;
        this.interval = usec;
        this.extended = extended;
    }

    public void startSending(Bus bus) {
        this.bus = bus;
        sending = true;
        /* Connection present -> remote sending */
        if(bus.getConnection() != null) {
            local = false;
            startRemoteSending();
        } else {
            local = true;
            startLocalSending();
        }
    }

    public void stopSending() throws InterruptedException {
        sending = false;
        if(local) {
            stopLocalSending();
        } else {
            stopRemoteSending();
        }
    }

    private void startLocalSending() {
        thread = new Thread(runnable);
        thread.start();
    }

    private void stopLocalSending() throws InterruptedException {
        if(thread != null && thread.isAlive()) {
            thread.interrupt();
            thread.join();
        }
    }

    private void startRemoteSending() {
        bus.addSendJob(id, extended, data, interval);
    }

    private void stopRemoteSending() {
        bus.removeSendJob(id, extended);
    }

}
