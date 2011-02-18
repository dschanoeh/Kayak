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

import com.github.kayak.core.BusURL;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    public void addRecent(BusURL beacon) {
        recent.add(beacon);
        notifyListeners();
    }

    public void removeRecent(BusURL url) {
        recent.remove(url);
        notifyListeners();
    }
    
    public void loadFromFile(InputStream stream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(stream);

            NodeList favouritesList = doc.getElementsByTagName("Favourites");
            if(favouritesList.getLength()==1) {
                Node favouritesNode = favouritesList.item(0);
                NodeList favourites = favouritesNode.getChildNodes();

                for(int i=0;i<favourites.getLength();i++) {
                    try {
                        NamedNodeMap attributes = favourites.item(i).getAttributes();
                        Node nameNode = attributes.getNamedItem("name");
                        String name = nameNode.getNodeValue();
                        Node hostNode = attributes.getNamedItem("host");
                        String host = hostNode.getNodeValue();
                        Node portNode = attributes.getNamedItem("port");
                        int port = Integer.parseInt(portNode.getNodeValue());

                        this.favourites.add(new BusURL(host, port, name));
                    } catch (Exception ex) {
                        
                    }
                }
            }

        } catch (Exception ex) {
            logOutput.getErr().write("Error while reading connections from file\n");
        }
    }

    public void writeToFile(OutputStream stream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("Connections");
            doc.appendChild(root);

            Element favouritesElement = doc.createElement("Favourites");
            for (BusURL url : favourites) {
                try {
                    Element favourite = doc.createElement("Connection");
                    favourite.setAttribute("host", url.getHost());
                    favourite.setAttribute("port", Integer.toString(url.getPort()));
                    favourite.setAttribute("name", url.getName());
                    favouritesElement.appendChild(favourite);
                } catch (Exception ex) {
                }
            }
            root.appendChild(favouritesElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
        } catch (Exception ex) {
            logOutput.getErr().write("Error while writing connections to file\n");
        }
    }
}
