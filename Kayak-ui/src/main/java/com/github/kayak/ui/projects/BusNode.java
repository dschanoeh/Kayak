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
import com.github.kayak.ui.rawview.OpenRawViewAction;
import com.github.kayak.ui.statistics.OpenBusStatisticsAction;
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

    private BusChangeListener changeListener = new BusChangeListener() {

        @Override
        public void connectionChanged() {

        }

        @Override
        public void nameChanged(String newName) {
            setDisplayName(bus.toString());
        }

        @Override
        public void destroyed() {

        }

        @Override
        public void descriptionChanged() {

        }

        @Override
        public void aliasChanged(String newAlias) {
            setDisplayName(bus.toString());
        }
    };

    public BusNode(Bus bus, Project project) {
        super(new BusChildFactory(bus, project), Lookups.fixed(bus, project));

        setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/places/network-workgroup.png");
        super.setDisplayName(bus.toString());
        this.bus = bus;
        this.project = project;
        bus.addBusChangeListener(changeListener);
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
    public Action[] getActions(boolean context) {
        return new Action[] { new OpenRawViewAction(bus), new OpenBusStatisticsAction(bus), new ChangeAliasAction(), new ChangeNameAction(), new DeleteBusAction() };
    }

    private class ChangeNameAction extends AbstractAction {

        public ChangeNameAction() {
            putValue(NAME, "Change (internal) name...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog("Please input a new name for the bus", bus.getName());

            if(name!=null && !project.isBusNameValid(name)) {
                while(true) {
                    name = JOptionPane.showInputDialog("Invalid bus name (a bus with that name does already exist or name does not match the rules)", bus.getName());
                    if(name == null || project.isBusNameValid(name))
                        break;
                }
            }

            if (name != null) {
                bus.setName(name);
            }
        }

    };

    private class ChangeAliasAction extends AbstractAction {

        public ChangeAliasAction() {
            putValue(NAME, "Change alias...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String alias = JOptionPane.showInputDialog("Please input a new alias for the bus", bus.getAlias());

            if (alias != null) {
                bus.setAlias(alias);
                setDisplayName(bus.toString());
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
