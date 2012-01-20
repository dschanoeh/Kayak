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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConnectionManager {
    private static ConnectionManager globalManager;
    private final Set<BusURL> favourites = Collections.synchronizedSet(new HashSet<BusURL>());
    private final Set<BusURL> recent = Collections.synchronizedSet(new HashSet<BusURL>());
    private final Set<BusURL> autoDiscovery = Collections.synchronizedSet(new HashSet<BusURL>());
    private Thread discoveryThread;
    private ArrayList<ConnectionListener> listeners;
    private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());

    private Runnable discoveryRunnable = new Runnable() {

        @Override
        public void run() {
            InetSocketAddress address = new InetSocketAddress(42000);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                logger.log(Level.WARNING, "Could not create XML parser. Aborting auto discovery.", ex);
                return;
            }

            while (true) {
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket(address);
                    socket.setSoTimeout(1000);

                } catch (SocketException ex) {
                    logger.log(Level.WARNING, "Could not create auto discovery socket. retrying...", ex);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex1) {
                        return;
                    }
                    continue;
                }

                while (true) {
                    /* Check if old beacons need to be removed from the list */
                    long currentTime = System.currentTimeMillis();
                    boolean removed = false;
                    synchronized(autoDiscovery) {
                        ArrayList<BusURL> toRemove = new ArrayList<BusURL>();
                        for(BusURL url : autoDiscovery) {
                            if(url.getTimestamp() < (currentTime - 5000)) {
                                toRemove.add(url);
                                removed = true;
                            }
                        }
                        autoDiscovery.removeAll(toRemove);
                    }

                    if(removed)
                        notifyListeners();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(packet);
                    } catch(SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        logger.log(Level.WARNING, "IO error in auto discovery socket.", ex);
                        continue;
                    }

                    StringBuilder sb = new StringBuilder(packet.getLength());
                    for(int i=0;i<packet.getLength();i++) {
                        sb.append((char) buffer[i]);
                    }
                    String string = sb.toString();

                    String url = null;
                    String description = null;
                    String hostName = null;
                    ArrayList<String> busses = new ArrayList<String>();

                    StringReader reader = new StringReader(string);
                    InputSource source = new InputSource(reader);
                    Document doc;
                    try {
                        doc = db.parse(source);
                    } catch (SAXException ex) {
                        logger.log(Level.WARNING, "Malformed discovery beacon", ex);
                        continue;
                    } catch (IOException ex) {
                        logger.log(Level.WARNING, "IO error in auto discovery parser.", ex);
                        continue;
                    }

                    Element root = doc.getDocumentElement();

                    if (root.getNodeName().equals("CANBeacon")) {
                        NamedNodeMap attributes = root.getAttributes();

                        Node descriptionNode = attributes.getNamedItem("description");
                        if(descriptionNode != null)
                            description = descriptionNode.getNodeValue();

                        Node hostNameNode = attributes.getNamedItem("name");
                        if(hostNameNode != null)
                            hostName = hostNameNode.getNodeValue();

                        NodeList children = root.getChildNodes();

                        for (int i = 0; i < children.getLength(); i++) {
                            Node child = children.item(i);

                            if (child.getNodeName().equals("URL")) {
                                url = child.getTextContent();
                            } else if (child.getNodeName().equals("Bus")) {
                                busses.add(child.getAttributes().getNamedItem("name").getNodeValue());
                            }
                        }
                    }

                    if (url != null) {
                        for (String bus : busses) {
                            String newURL = "socket://" + bus + "@" + url.substring(6);
                            BusURL busURL = BusURL.fromString(newURL);
                            busURL.setTimestamp(System.currentTimeMillis());

                            if(hostName != null)
                                busURL.setHostName(hostName);

                            if(description != null)
                                busURL.setDescription(description);

                            boolean changed = false;
                            synchronized(autoDiscovery) {
                                /* If the beacon is not in the list add it*/
                                if(!autoDiscovery.contains(busURL)) {
                                    autoDiscovery.add(busURL);
                                    changed = true;
                                /* otherwise update timestamp */
                                } else {
                                    for(BusURL old : autoDiscovery) {
                                        if(old.equals(busURL)) {
                                            old.setTimestamp(System.currentTimeMillis());
                                        }
                                    }
                                }
                            }
                            if(changed)
                                notifyListeners();
                        }
                    }
                }
            }
        }
    };

    public Set<BusURL> getAutoDiscovery() {
        synchronized(autoDiscovery) {
            return Collections.unmodifiableSet(autoDiscovery);
        }
    }

    public Set<BusURL> getFavourites() {
        synchronized(favourites) {
            return Collections.unmodifiableSet(favourites);
        }
    }

    public Set<BusURL> getRecent() {
        synchronized(recent) {
            return Collections.unmodifiableSet(recent);
        }
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
                logger.log(Level.WARNING, "URL already in favourites!");
                return;
            }
        }

        BusURL url2 = new BusURL(url.getHost(), url.getPort(), url.getBus());
        logger.log(Level.INFO, "adding favourite: {0}", url2.toString());
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
            logger.log(Level.SEVERE, "Error while reading connections from file\n");
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
                    favourite.setAttribute("name", url.getBus());
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
            logger.log(Level.SEVERE, "Error while writing connections to file\n");
        }
    }
}
