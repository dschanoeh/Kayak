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
package com.github.kayak.logging.snapshots;

import java.util.ArrayList;

import com.github.kayak.core.*;
import com.github.kayak.core.TimeSource.Mode;
import com.github.kayak.ui.time.TimeSourceManager;
import java.io.File;

/**
 * A SnapshotBuffer can be connected to a {@link Bus} and then buffers
 * all {@link Frame}s that are on the bus. Therefore it adds a RAW
 * subscription. After receiving Frames the Buffer can be connected to
 * a different (or the same) bus to replay the frames.
 * The depth of the buffer may be set and is per default 5 seconds.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class SnapshotBuffer {

    private int depth = 5000;
    private Subscription subscription;
    private final ArrayList<Frame> frames;
    private Thread cleanupThread;
    private Thread replayThread;
    private int stopTimeout = 0;
    private boolean stopRequest = false;
    private Mode mode;
    private TimeSource timeSource;
    private ArrayList<Bus> busses;
    private ArrayList<Subscription> subscriptions;
    private String name;
    private String platform;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    private FrameReceiver receiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame frame) {
            synchronized (frames) {
                frames.add(frame);
            }
        }
    };

    private Runnable cleanupRunnable = new Runnable() {

        @Override
        public void run() {
            while (!stopRequest) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    if (stopRequest) {
                        try {
                            Thread.sleep(stopTimeout);
                        } catch (InterruptedException ex) {}

                        for(Subscription s : subscriptions) {
                            s.Terminate();
                        }
                        subscriptions.clear();
                        busses.clear();
                    }
                }
                long currentTime = TimeSourceManager.getGlobalTimeSource().getTime();
                synchronized (frames) {
                    for (Frame f : frames) {
                        if (f.getTimestamp() < currentTime - depth) {
                            frames.remove(f);
                        }
                    }
                }
            }
        }
    };

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (depth > 0) {
            this.depth = depth;
        }
    }

    public SnapshotBuffer() {
        frames = new ArrayList<Frame>(500);
        busses = new ArrayList<Bus>();
        subscriptions = new ArrayList<Subscription>();
    }

    public void connectBus(Bus bus) {
        busses.add(bus);
        Subscription s = new Subscription(receiver, bus);
        bus.addSubscription(s);
        subscriptions.add(s);
    }

    /**
     * Connect the buffer to a bus to receive frames. This is done through
     * a RAW subscription that is added to the bus.
     * @param bus The bus to receive Frames from
     */
    public void startBuffering() {

        frames.clear();
        for(Subscription s : subscriptions) {
            s.setSubscribeAll(true);
        }

        stopRequest = false;
        cleanupThread = new Thread(cleanupRunnable);
        cleanupThread.start();
    }

    /**
     * Stop buffering from the bus. No more frames will be added. The
     * connection to the bus is terminated.
     */
    public void stopBuffering(int ms) {
        stopTimeout = ms;
        stopRequest = true;

        if (cleanupThread != null && cleanupThread.isAlive()) {
            replayThread.interrupt();
        }
    }

    public void writeToFile(File f) {

    }
}
