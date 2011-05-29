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
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.ui.connections.ConnectionManager;
import com.github.kayak.ui.descriptions.DescriptionNode;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class BusChildFactory extends Children.Keys<BusChildFactory.Folders> {

    private BusChangeListener listener = new BusChangeListener() {

        @Override
        public void connectionChanged() {
            addNotify();
        }

        @Override
        public void nameChanged() {
            
        }

        @Override
        public void destroyed() {
            
        }

        @Override
        public void descriptionChanged() {
            
        }
    };  

    public enum Folders {
        CONNECTION, DESCRIPTION;
    }
    private Bus bus;

    public BusChildFactory(Bus bus) {
        this.bus = bus;

        bus.addBusChangeListener(listener);
    }

    @Override
    public void addNotify() {
        setKeys(new Folders[] {Folders.CONNECTION, Folders.DESCRIPTION});
    }

    @Override
    protected Node[] createNodes(Folders key) {
        if (key == Folders.CONNECTION) {

            AbstractNode node = new ConnectionFolderNode(Children.create(new ConnectionChildFactory(bus), false), bus);
            node.setDisplayName("Connection");
            node.setIconBaseWithExtension("org/freedesktop/tango/16x16/devices/network-wired.png");

            return new Node[]{node};
        } else if (key == Folders.DESCRIPTION) {
            return new Node[]{ new DescriptionFolderNode() };
        }

        return null;
    }
    
    private class DescriptionFolderNode extends AbstractNode {
        
        public DescriptionFolderNode() {
            super(Children.create(new DescriptionChildFactory(bus), true));
            setDisplayName("Description");
        }
        
        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            try {
                final BusDescription desc = (BusDescription) t.getTransferData(DescriptionNode.DATA_FLAVOR);
                return new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        bus.setDescription(desc);
                        return null;
                    }
                };
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }

    private class ConnectionFolderNode extends AbstractNode {

        public ConnectionFolderNode(Children children, Bus bus) {
            super(children, Lookups.fixed(bus));
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            try {
                final BusURL url = (BusURL) t.getTransferData(BusURL.DATA_FLAVOR);
                return new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        if(url.checkConnection()) {
                            bus.setConnection(url);
                            ConnectionManager.getGlobalConnectionManager().addRecent(url);
                        }
                        return null;
                    }
                };
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
}
