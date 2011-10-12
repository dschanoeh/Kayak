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

import com.github.kayak.ui.time.TimeSourceManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.github.kayak.core.Bus;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class Project {

    private final Set<Bus> busses = Collections.synchronizedSet(new HashSet<Bus>());
    private String name;
    private HashSet<ProjectChangeListener> listeners = new HashSet<ProjectChangeListener>();
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

    public boolean isBusNameValid(String name) {
        if(!Bus.BUS_NAME_PATTERN.matcher(name).matches())
            return false;

        synchronized (busses) {
            for (Bus bus : busses) {
                if (name.equals(bus.getName())) {
                    return false;
                }
            }
        }

        return true;
    }

    public String getNextValidBusName() {
        String n = "";
        for(int i=0;;i++) {
            n = "can" + String.valueOf(i);
            if(isBusNameValid(n)) {
                break;
            }
        }

        return n;
    }

    public void addProjectChangeListener(ProjectChangeListener listener) {
        listeners.add(listener);
    }

    public void removeProjectChangeListener(ProjectChangeListener listener) {
        listeners.remove(listener);
    }

    public Set<Bus> getBusses() {
        return Collections.unmodifiableSet(busses);
    }

    public void addBus(Bus b) {
        if(b == null)
            return;

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
    }

}
