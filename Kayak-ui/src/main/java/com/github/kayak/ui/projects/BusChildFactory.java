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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
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

            AbstractNode node = new AbstractNodeImpl(Children.create(new ConnectionChildFactory(bus), false));
            node.setDisplayName("Connection");
            node.setIconBaseWithExtension("com/github/kayak/ui/projects/network-wired.png");

            return new Node[]{node};
        } else if (key == Folders.DESCRIPTION) {

            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setDisplayName("Description");

            return new Node[]{node};
        } else if (key == Folders.INPUT) {

            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setDisplayName("Log input");
            node.setIconBaseWithExtension("com/github/kayak/ui/projects/go-previous.png");

            return new Node[]{node};
        }else if (key == Folders.OUTPUT) {

            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setDisplayName("Log output");
            node.setIconBaseWithExtension("com/github/kayak/ui/projects/go-next.png");

            return new Node[]{node};
        }

        return null;
    }

    private class AbstractNodeImpl extends AbstractNode {

        public AbstractNodeImpl(Children children) {
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
}
