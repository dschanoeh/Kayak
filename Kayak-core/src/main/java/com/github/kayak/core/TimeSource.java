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

/**
 * This class is used to synchronize the time between different busses or other
 * classes. After a TimeSource is created it can be connected to any number of
 * objects. Objects can either simply call getTime() to receive the time or
 * register themselves to receive TimeEvents via the {@link TimeEventReceiver}
 * interface. This can be used to synchronize multiple components.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class TimeSource {

    private static final int MODE_STOP = 0;
    private static final int MODE_PLAY = 1;
    private static final int MODE_PAUSE = 2;
    private long reference;
    private long pauseReference;
    private int mode = MODE_STOP;
    private ArrayList<TimeEventReceiver> receivers;
    
    public TimeSource() {
        reference = System.currentTimeMillis();
    }
    
    public void reset() {
        reference = System.currentTimeMillis();
    }

    /**
     * Returns the time (in milliseconds) since the creation of the TimeSource
     * (or since the last stop). A static value is returned if the TimeSource 
     * is paused. If the TimeSource is stopped '0' is returned.
     */
    public long getTime() {
        switch(mode) {
            case MODE_PLAY:
                return System.currentTimeMillis() - reference;
            
            case MODE_STOP:
                return 0;
                    
            case MODE_PAUSE:
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
        receivers.add(receiver);
    }
    
    public void deregister(TimeEventReceiver receiver) {
        receivers.remove(receiver);
    }
    
    /**
     * If the TimeSource is not already running starts the time. If the
     */
    public void play() {
        mode = MODE_PLAY;
        
        for(TimeEventReceiver receiver : receivers) {
            if(receiver != null)
                receiver.played();
        }
    }
    
    /**
     * Pauses the time.
     */
    public void pause() {
        mode = MODE_PAUSE;
        
        pauseReference = System.currentTimeMillis() - reference;
        
        for(TimeEventReceiver receiver : receivers) {
            if(receiver != null)
                receiver.paused();
        }
    }
    
    public void stop() {
        mode = MODE_STOP;
        
        for(TimeEventReceiver receiver : receivers) {
            if(receiver != null)
                receiver.stopped();
        }
    }
}
