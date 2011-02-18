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
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class ConnectionManager {
    private static ConnectionManager globalManager;
    private ArrayList<BusURL> favourites;
    private ArrayList<BusURL> recent;
    private ArrayList<BusURL> autoDiscovery;
    private Thread discoveryThread;
    private ArrayList<ConnectionListener> listeners;
    private static InputOutput logOutput = IOProvider.getDefault().getIO("Connections", false);
    
    private Runnable discoveryRunnable = new Runnable() {

        @Override
        public void run() {
            autoDiscovery.add(BusURL.fromString("socket://can0@127.0.0.1:28600"));
            notifyListeners();
            int i=0;
            while(true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                autoDiscovery.add(BusURL.fromString("socket://can0@129.0.0.1:"+Integer.toString(i)));
                notifyListeners();
                i++;
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

    /**
     * Add a @link{BusURL} to the favourites. A new instance with the same
     * parameters is created. If the favourites already contain this element
     * nothing is added.
     * @param url
     */
    public void addFavourite(BusURL url) {
        
        /* no duplicates */
        for(BusURL u : favourites) {
            if(u.equals(url)) {
                logOutput.getErr().print("URL already in favourites!\n");
                return;
            }
        }

        BusURL url2 = new BusURL(url.getHost(), url.getPort(), url.getName());
        logOutput.getOut().print("adding favourite: " + url2.toString() + "\n");
        favourites.add(url2);
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

    public static ConnectionManager getGlobalConnectionManager() {
        if(globalManager == null)
            globalManager = new ConnectionManager();
        return globalManager;
    }

    private ConnectionManager() {
        favourites = new ArrayList<BusURL>();
        recent = new ArrayList<BusURL>();
        autoDiscovery = new ArrayList<BusURL>();

        discoveryThread = new Thread(discoveryRunnable);
        discoveryThread.start();

        listeners = new ArrayList<ConnectionListener>();
    }

    void addRecent(BusURL beacon) {
        recent.add(beacon);
        notifyListeners();
    }

    void removeRecent(BusURL url) {
        recent.remove(url);
        notifyListeners();
    }
}
