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

/**
 * This class is used to synchronize the time between different busses or other
 * classes. After a TimeSource is created it can be connected to any number of
 * objects. Objects can either simply call getTime() to receive the time or
 * register themselves to receive TimeEvents via the {@link TimeEventReceiver}
 * interface. This can be used to synchronize multiple components.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class TimeSource {

    public static enum Mode { STOP, PLAY, PAUSE };

    private long reference;
    private long pauseReference;
    private Mode mode = Mode.STOP;
    private final Set<TimeEventReceiver> receivers= Collections.synchronizedSet(new HashSet<TimeEventReceiver>());

    public Mode getMode() {
        return mode;
    }

    public TimeSource() {
        reference = System.nanoTime()/1000000;
    }

    public void reset() {
        reference = System.nanoTime()/1000000;
    }

    /**
     * Returns the time (in milliseconds) since the creation of the TimeSource
     * (or since the last stop). A static value is returned if the TimeSource
     * is paused. If the TimeSource is stopped '0' is returned.
     */
    public long getTime() {
        switch(mode) {
            case PLAY:
                return (System.nanoTime()/1000000) - reference;

            case STOP:
                return 0;

            case PAUSE:
                return pauseReference;

            default:
                return 0;
        }
    }

    /**
     * Register a {@link TimeEventReceiver} to receive information about
     * changes of the TimeSource.
     */
    public void register(TimeEventReceiver receiver) {
        synchronized(receivers) {
            receivers.add(receiver);
        }
    }

    public void deregister(TimeEventReceiver receiver) {
        synchronized(receivers) {
            receivers.remove(receiver);
        }
    }

    /**
     * If the TimeSource is not already running start the time. If the
     * TimeSource was paused before continue with the paused Time.
     */
    public void play() {
        if(mode == Mode.PLAY)
            return;
        else {
            if(mode == Mode.PAUSE)
                reference = (System.nanoTime()/1000000) - pauseReference;
            else
                reference = (System.nanoTime()/1000000);

            mode = Mode.PLAY;

            synchronized(receivers) {
                for(TimeEventReceiver receiver : receivers) {
                    if(receiver != null)
                        receiver.played();
                }
            }
        }
    }

    /**
     * Pauses the time.
     */
    public void pause() {
        if(mode == Mode.STOP)
            return;

        mode = Mode.PAUSE;

        pauseReference = (System.nanoTime()/1000000) - reference;

        synchronized(receivers) {
            for(TimeEventReceiver receiver : receivers) {
                if(receiver != null)
                    receiver.paused();
            }
        }
    }

    public void stop() {
        mode = Mode.STOP;

        synchronized(receivers) {
            for(TimeEventReceiver receiver : receivers) {
                if(receiver != null)
                    receiver.stopped();
            }
        }
    }
}
