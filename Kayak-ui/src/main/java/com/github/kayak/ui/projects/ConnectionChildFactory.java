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
import com.github.kayak.ui.connections.BusURLNode;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ConnectionChildFactory extends ChildFactory<BusURL> {

    private Bus bus;

    private BusChangeListener listener = new BusChangeListener() {

        @Override
        public void connectionChanged() {
            refresh(true);
        }

        @Override
        public void nameChanged() {
            
        }

        @Override
        public void destroyed() {
            
        }
    };

    public ConnectionChildFactory(Bus bus) {
        this.bus = bus;
        bus.addBusChangeListener(listener);
    }
    @Override
    protected boolean createKeys(List<BusURL> toPopulate) {
        BusURL connection = bus.getConnection();
        if(connection != null)
            toPopulate.add(connection);

        return true;
    }

    @Override
    protected Node[] createNodesForKey(BusURL key) {
        return new Node[] {new MyBusURLNode(key, bus)};
    }

    private class MyBusURLNode extends BusURLNode {
        private Bus bus;

        private class DisconnectAction extends AbstractAction {

            public DisconnectAction() {
                putValue(NAME, "Disconnect");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                bus.disconnect();
            }

        };

        public MyBusURLNode(BusURL url, Bus bus) {
            super(url, BusURLNode.Type.CONNECTED);
            this.bus = bus;
        }

        @Override
        public Action[] getActions(boolean popup) {
            return new Action[] {
              new DisconnectAction()
            };
        }
    };
}
