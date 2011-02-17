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

import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ConnectionChildrenFactory extends Children.Keys<BusURL> implements ConnectionListener {
    private ConnectionManager manager;
    private BusURLNode.Type type;

    @Override
    public void addNotify() {
        switch(type) {
            case DISCOVERY:
                setKeys(manager.getAutoDiscovery().toArray(new BusURL[0]));
                break;
            case FAVOURITE:
                setKeys(manager.getFavourites().toArray(new BusURL[0]));
                break;
            case RECENT:
                setKeys(manager.getRecent().toArray(new BusURL[0]));
                break;
        }
    }

    public ConnectionChildrenFactory(ConnectionManager manager, BusURLNode.Type type) {
        this.manager = manager;
        this.type = type;
        manager.addConnectionListener(this);
    }

    @Override
    protected Node[] createNodes(BusURL busURL) {
        return new Node[] {new BusURLNode(busURL, type, manager)};
    }

    @Override
    public void connectionsChanged() {
        addNotify();
    }

}
