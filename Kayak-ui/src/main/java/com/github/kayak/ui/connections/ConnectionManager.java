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

package com.github.kayak.ui.connections;

import java.util.ArrayList;
import org.openide.util.Exceptions;

public class ConnectionManager {
    private ArrayList<BusURL> favourites;
    private ArrayList<BusURL> recent;
    private ArrayList<BusURL> autoDiscovery;
    private Thread discoveryThread;
    private ArrayList<ConnectionListener> listeners;
    
    private Runnable discoveryRunnable = new Runnable() {

        @Override
        public void run() {
            autoDiscovery.add(BusURL.fromString("socket://can0@127.0.0.1:28600"));
            notifyListeners();

            while(true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                autoDiscovery.add(BusURL.fromString("socket://can0@129.0.0.1:28601"));
                notifyListeners();
            }
        }
    };

    public ArrayList<BusURL> getAutoDiscovery() {
        return autoDiscovery;
    }

    public ArrayList<BusURL> getFavourites() {
        return favourites;
    }

    public ArrayList<BusURL> getRecent() {
        return recent;
    }

    public void addFavourite(BusURL url) {
        favourites.add(url);
        notifyListeners();
    }

    public void removeFavourite(BusURL url) {
        favourites.remove(url);
        notifyListeners();
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for(ConnectionListener listener : listeners) {
            listener.connectionsChanged();
        }
    }

    public ConnectionManager() {
        favourites = new ArrayList<BusURL>();
        recent = new ArrayList<BusURL>();
        autoDiscovery = new ArrayList<BusURL>();

        discoveryThread = new Thread(discoveryRunnable);
        discoveryThread.start();

        listeners = new ArrayList<ConnectionListener>();
    }
}
