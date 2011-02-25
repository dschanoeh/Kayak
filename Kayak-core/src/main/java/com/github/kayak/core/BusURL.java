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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * URL class that absoulutely defines the connection parameters for a single bus
 * that is provided by a socketcand
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class BusURL implements Transferable {

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(BusURL.class, "BusURL");
    private String host;
    private String bus;
    private int port;
    private String hostName;
    private String description;
    private long timestamp;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    private BusURL() {
    }

    public BusURL(String host, int port, String bus) {
        this.host = host;
        this.port = port;
        this.bus = bus;
    }

    /**
     * Build a new BusURL from a String. The String must have the following
     * format: socket://bus\@host:port
     * @param s The String that shall be parsed
     * @return
     */
    public static BusURL fromString(String s) {
        if(s.matches("^socket\\://[a-zA-Z0-9]+\\@[a-zA-Z0-9\\-\\.]+\\:[0-9]+?$")) {
            BusURL b = new BusURL();
            int portSeparator = s.lastIndexOf(':');
            int at = s.indexOf("@");

            b.setPort(Integer.parseInt(s.substring(portSeparator+1)));
            b.setHost(s.substring(at +1, portSeparator));
            b.setBus(s.substring(9,at));

            return b;
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(this.hashCode() == obj.hashCode() && obj instanceof BusURL)
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 47 * hash + (this.bus != null ? this.bus.hashCode() : 0);
        hash = 47 * hash + this.port;
        return hash;
    }


    @Override
    public String toString() {
        String s = "";
        if(bus != null)
            s += bus;

        if(hostName != null)
            s += "@" + hostName;
        else
            s += "@" + host + ":" + Integer.toString(port);

        if(description != null)
            s += " (" + description + ")";

        return s;
    }

    public String toURLString() {
        return  "socket://" + bus + "@" + host + ":" + Integer.toString(port);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(flavor == DATA_FLAVOR) {
            return this;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public Boolean checkConnection() {
        Socket socket = new Socket();
        InetSocketAddress address = new InetSocketAddress(host, port);

        try {
            socket.setSoTimeout(10);
            socket.connect(address, 50);

            InputStreamReader input = new InputStreamReader(
                    socket.getInputStream());

            /*String ret = "";

            for(int i=0;i<)

            if (!ret.equals("< hi >")) {
                logger.log(Level.SEVERE, "Did not receive greeting from host.");
            }*/
        } catch (IOException ex) {
            return false;
        }

        return true;
    }
}
