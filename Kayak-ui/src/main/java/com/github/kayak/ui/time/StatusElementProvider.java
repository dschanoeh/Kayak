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
package com.github.kayak.ui.time;

import com.github.kayak.core.TimeEventReceiver;
import com.github.kayak.core.TimeSource;
import java.awt.Component;
import javax.swing.JLabel;
import org.openide.awt.StatusLineElementProvider;

/**
 * Provides a StatusLineElement that shows the state of the global 
 * {@link TimeSource}
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
@org.openide.util.lookup.ServiceProvider(service=StatusLineElementProvider.class)
public class StatusElementProvider implements StatusLineElementProvider {
    
    private TimeSource source;
    private TimeSource.Mode mode;
    private JLabel label;
    private Thread thread;
    
    private TimeEventReceiver receiver = new TimeEventReceiver() {

        @Override
        public void paused() {
            mode = TimeSource.Mode.PAUSE;
        }

        @Override
        public void played() {
            mode = TimeSource.Mode.PLAY;
        }

        @Override
        public void stopped() {
            mode = TimeSource.Mode.STOP;
        }
    };
    
    private Runnable myRunnable = new Runnable() {

        @Override
        public void run() {
            while(true) {
                switch(mode) {
                    case PAUSE:
                        label.setText("Time paused (" + Long.toString(source.getTime()) + ")");
                        break;
                    case PLAY:
                        label.setText("Time running (" + Long.toString(source.getTime()) + ")");
                        break;
                    case STOP:
                        label.setText("Time stopped (" + Long.toString(source.getTime()) + ")");
                        break;
                }
                
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }
    };
    
    public StatusElementProvider() {
        label = new JLabel();
        source = TimeSourceManager.getGlobalTimeSource();
        mode = source.getMode();
        source.register(receiver);
        
        thread = new Thread(myRunnable);
        thread.setName("Status bar time refresh");
        thread.start();
    }
    
    @Override
    public Component getStatusLineElement() {
        return label;
    }
}
