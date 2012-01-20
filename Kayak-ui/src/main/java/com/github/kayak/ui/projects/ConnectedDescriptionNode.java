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
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.ui.descriptions.DescriptionNode;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dschanoeh
 */
public class ConnectedDescriptionNode extends AbstractNode {

    private Bus bus;
    private BusDescription description = null;

    private BusChangeListener listener = new BusChangeListener() {

        @Override
        public void connectionChanged() {
        }

        @Override
        public void nameChanged(String name) {
        }

        @Override
        public void destroyed() {
        }

        @Override
        public void descriptionChanged() {
            BusDescription newDesc = bus.getDescription();
            description = newDesc;

            if(newDesc == null) {
                setDisplayName("Description: None");
            } else {
                setDisplayName("Description: " + description.getName());
            }
        }

        @Override
        public void aliasChanged(String string) {

        }
    };

    public ConnectedDescriptionNode(Bus bus) {
        super(Children.LEAF, Lookups.fixed(bus));

        this.bus = bus;
        bus.addBusChangeListener(listener);
        description = bus.getDescription();
        if(description == null)
            setDisplayName("Description: None");
        else
            setDisplayName("Description: " + description.getName());

        setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/mimetypes/text-x-generic.png");
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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean popup) {
        if(description != null)
            return new Action[] { new DisconnectAction() };
        else
            return new Action[]{};
    }

    private class DisconnectAction extends AbstractAction {

        public DisconnectAction() {
            putValue(NAME, "Disconnect");
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            bus.setDescription(null);
        }

    };

}
