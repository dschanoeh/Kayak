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

package com.github.kayak.ui.projects;

import com.github.kayak.core.*;
import com.github.kayak.ui.time.TimeSourceManager;
import java.util.ArrayList;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class Project {
    
    private ArrayList<Bus> busses;
    private String name;
    private ArrayList<ProjectChangeListener> listeners;
    private boolean opened = false;

    public boolean isOpened() {
        return opened;
    }

    public void open() {
        this.opened = true;
        
        for(Bus b : busses) {
            b.setTimeSource(TimeSourceManager.getGlobalTimeSource());
        }
        notifyListenersOpened();
    }
    
    public void close() {
        this.opened = false;
        
        for(Bus b : busses) {
            b.setTimeSource(null);
            b.destroy();
        }
        
        notifyListenersClosed();
    }

    public void addProjectChangeListener(ProjectChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeProjectChangeListener(ProjectChangeListener listener) {
        listeners.remove(listener);
    }

    public ArrayList<Bus> getBusses() {
        return busses;
    }

    public void addBus(Bus b) {
        busses.add(b);
        if(isOpened()) {
            b.setTimeSource(TimeSourceManager.getGlobalTimeSource());
        }
        for(ProjectChangeListener listener : listeners) {
            listener.projectBusAdded(this, b);
        }
    }

    public void removeBus(Bus b) {
        busses.remove(b);
        if(isOpened()) {
            b.setTimeSource(null);
        }
        for(ProjectChangeListener listener : listeners) {
            listener.projectBusRemoved(this, b);
        }
    }

    public String getName() {
        return name;
    }

     public void setName(String name) {
        this.name = name;
        notifyListenersName();
    }

    private void notifyListenersName() {
        for(ProjectChangeListener listener : listeners) {
            listener.projectNameChanged(this, getName());
        }
    }

    private void notifyListenersClosed() {
        ProjectChangeListener[] listenerArray = listeners.toArray(new ProjectChangeListener[listeners.size()]);
        for(ProjectChangeListener listener : listenerArray) {
            listener.projectClosed(this);
        }
    }

    private void notifyListenersOpened() {
        ProjectChangeListener[] listenerArray = listeners.toArray(new ProjectChangeListener[listeners.size()]);
        for(ProjectChangeListener listener : listenerArray) {
            listener.projectOpened(this);
        }
    }

    public Project(String name) {
        this.name = name;
        busses = new ArrayList<Bus>();
        listeners = new ArrayList<ProjectChangeListener>();
    }


}
