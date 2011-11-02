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

import com.github.kayak.core.Bus;
import com.github.kayak.core.BusChangeListener;
import com.github.kayak.core.BusURL;
import com.github.kayak.ui.connections.BookmarkConnectionAction;
import com.github.kayak.ui.connections.ConnectionManager;
import com.github.kayak.ui.useroutput.UserOutput;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dschanoeh
 */
public class ConnectedBusURLNode extends AbstractNode {

    private static final Logger logger = Logger.getLogger(ConnectedBusURLNode.class.getCanonicalName());

    private Project project;
    private BusURL url;
    private Bus bus;

    private BusChangeListener listener = new BusChangeListener() {

        @Override
        public void connectionChanged() {
            BusURL conn = bus.getConnection();
            url = conn;
            if(conn == null) {
                setDisplayName("Connection: None");
            } else {
                setDisplayName("Connection: " + url.toString());
            }
        }

        @Override
        public void nameChanged(String name) {
        }

        @Override
        public void destroyed() {
        }

        @Override
        public void descriptionChanged() {
        }

        @Override
        public void aliasChanged(String string) {

        }
    };

    public ConnectedBusURLNode(BusURL url, Bus bus, Project project) {
        super(Children.LEAF, Lookups.fixed(bus));

        this.url = url;
        this.bus = bus;
        this.project = project;

        bus.addBusChangeListener(listener);

        if(url == null) {
            setDisplayName("Connection: None");
        } else {
            setDisplayName("Connection: " + url.toString());
        }
        setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/devices/network-wired.png");
    }

    @Override
    public Action[] getActions(boolean popup) {
        if(url != null)
            return new Action[] { new DisconnectAction(), new BookmarkConnectionAction(url) };
        else
            return new Action[]{};
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = s.createPropertiesSet();

        Property host = new PropertySupport.ReadOnly<String>("Host", String.class, "Host", "The host of the connection") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return url.getHost();
            }

        };

        Property port = new PropertySupport.ReadOnly<Integer>("Port", Integer.class, "Port", "Port of the connection") {

            @Override
            public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                return url.getPort();
            }

        };

        Property bus = new PropertySupport.ReadOnly<String>("Bus name", String.class, "Bus name", "Name of the bus on the host") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return url.getBus();
            }

        };

        Property description = new PropertySupport.ReadOnly<String>("Description", String.class, "Description", "Human readable description of the socketcand service") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return url.getDescription();
            }

        };

        Property hostName = new PropertySupport.ReadOnly<String>("Host name", String.class, "Host name", "The name of the machine the socketcand is running on") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return url.getHostName();
            }

        };

        set.put(host);
        set.put(port);
        set.put(bus);
        set.put(description);
        set.put(hostName);

        s.put(set);

        return s;
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        try {
            final BusURL newURL = (BusURL) t.getTransferData(BusURL.DATA_FLAVOR);
            return new PasteType() {

                @Override
                public Transferable paste() throws IOException {
                    if(newURL.checkConnection()) {
                        url = newURL;

                        String newName = url.getBus();
                        if(project.isBusNameValid(newName)) {
                            bus.setName(url.getBus());
                        }

                        bus.setConnection(url);
                        ConnectionManager.getGlobalConnectionManager().addRecent(url);
                    } else {
                        UserOutput.printWarning("Could not connect to socketcand! Check if the host is up.");
                    }
                    return null;
                }
            };
        } catch (UnsupportedFlavorException ex) {
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private class DisconnectAction extends AbstractAction {

        public DisconnectAction() {
            putValue(NAME, "Disconnect");
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            bus.setConnection(null);
        }

    };
}
