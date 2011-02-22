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
import com.github.kayak.ui.logfiles.LogFileBusTupel;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class BusChildFactory extends Children.Keys<BusChildFactory.Folders> implements BusChangeListener {

    @Override
    public void connectionChanged() {
        addNotify();
    }

    public enum Folders {
        CONNECTION, DESCRIPTION, INPUT, OUTPUT;
    }
    private Bus bus;

    public BusChildFactory(Bus bus) {
        this.bus = bus;

        bus.addBusChangeListener(this);
    }

    @Override
    public void addNotify() {
        setKeys(new Folders[] {Folders.CONNECTION, Folders.DESCRIPTION, Folders.INPUT, Folders.OUTPUT});
    }

    @Override
    protected Node[] createNodes(Folders key) {
        if (key == Folders.CONNECTION) {

            AbstractNode node = new ConnectionFolderNode(Children.create(new ConnectionChildFactory(bus), false));
            node.setDisplayName("Connection");
            node.setIconBaseWithExtension("org/freedesktop/tango/16x16/devices/network-wired.png");

            return new Node[]{node};
        } else if (key == Folders.DESCRIPTION) {

            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setDisplayName("Description");

            return new Node[]{node};
        } else if (key == Folders.INPUT) {

            AbstractNode node = new LogFileFolderNode(Children.LEAF);
            node.setDisplayName("Log input");
            node.setIconBaseWithExtension("org/freedesktop/tango/16x16/actions/go-previous.png");

            return new Node[]{node};
        }else if (key == Folders.OUTPUT) {

            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setDisplayName("Log output");
            node.setIconBaseWithExtension("org/freedesktop/tango/16x16/actions/go-next.png");

            return new Node[]{node};
        }

        return null;
    }

    private class ConnectionFolderNode extends AbstractNode {

        public ConnectionFolderNode(Children children) {
            super(children);
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            try {
                final BusURL url = (BusURL) t.getTransferData(BusURL.DATA_FLAVOR);
                return new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        bus.setConnection(url);
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

    private class LogFileFolderNode extends AbstractNode {

        public LogFileFolderNode(Children children) {
            super(children);
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            try {
                final LogFileBusTupel tupel = (LogFileBusTupel) t.getTransferData(LogFileBusTupel.DATA_FLAVOR);
                return new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        
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
