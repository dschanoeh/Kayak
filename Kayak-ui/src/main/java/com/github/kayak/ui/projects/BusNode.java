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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.AbstractNode;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class BusNode extends AbstractNode implements Transferable {

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(BusNode.class, "BusNode");

    private Bus bus;
    private Project project;

    public BusNode(Bus bus, Project project) {
        super(new BusChildFactory(bus), Lookups.fixed(bus));

        setIconBaseWithExtension("org/freedesktop/tango/16x16/places/network-workgroup.png");
        super.setDisplayName(bus.getName());
        this.bus = bus;
        this.project = project;
    }

    public Bus getBus() {
        return bus;
    }

    @Override
    public Transferable drag() {
        return this;
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
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (flavor == DATA_FLAVOR) {
            return this;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public void setDisplayName(String s) {
        super.setDisplayName(s);
        bus.setName(s);
    }

    @Override
    public Action[] getActions(boolean context) {

        return new Action[] { new RenameBusAction(), new DeleteBusAction() };
    }

    private class RenameBusAction extends AbstractAction {

        public RenameBusAction() {
            putValue(NAME, "Rename...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog("Please give a new name for the bus", bus.getName());

            if (name != null) {
                setDisplayName(name);
            }
        }

    };

    private class DeleteBusAction extends AbstractAction {

        public DeleteBusAction() {
            putValue(NAME, "Delete");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove the bus?", "Are you sure?", JOptionPane.YES_NO_OPTION);

            if(result == JOptionPane.YES_OPTION) {
                project.removeBus(bus);
                bus.destroy();
            }

        }

    };
};
