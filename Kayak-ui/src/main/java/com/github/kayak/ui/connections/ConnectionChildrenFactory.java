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

import com.github.kayak.core.BusURL;
import java.util.Set;
import java.util.TreeSet;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Creates the children in a connection folder according to the given folder
 * type. The {@link ConnectionManager} is asked for the elements.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ConnectionChildrenFactory extends Children.Keys<BusURL> implements ConnectionListener {
    private ConnectionManager manager = ConnectionManager.getGlobalConnectionManager();
    private BusURLNode.Type type;

    @Override
    public void addNotify() {
        switch(type) {
            case DISCOVERY:
               TreeSet<BusURL> set = new TreeSet<BusURL>(BusURL.nameComparator);
               set.addAll(manager.getAutoDiscovery());
               setKeys(set.toArray(new BusURL[set.size()]));
               break;
            case FAVOURITE:
               TreeSet<BusURL> set1 = new TreeSet<BusURL>(BusURL.nameComparator);
               set1.addAll(manager.getFavourites());
               setKeys(set1.toArray(new BusURL[set1.size()]));
               break;
            case RECENT:
               TreeSet<BusURL> set2 = new TreeSet<BusURL>(BusURL.nameComparator);
               set2.addAll(manager.getRecent());
               setKeys(set2.toArray(new BusURL[set2.size()]));
               break;
        }
    }

    public ConnectionChildrenFactory(BusURLNode.Type type) {
        this.type = type;
        manager.addConnectionListener(this);
    }

    @Override
    protected Node[] createNodes(BusURL busURL) {
        return new Node[] {new BusURLNode(busURL, type)};
    }

    @Override
    public void connectionsChanged() {
        switch(type) {
            case DISCOVERY:
                TreeSet<BusURL> set = new TreeSet<BusURL>(BusURL.nameComparator);
                set.addAll(manager.getAutoDiscovery());
                setKeys(set.toArray(new BusURL[set.size()]));
                break;
            case FAVOURITE:
                TreeSet<BusURL> set1 = new TreeSet<BusURL>(BusURL.nameComparator);
                set1.addAll(manager.getFavourites());
                setKeys(set1.toArray(new BusURL[set1.size()]));
                break;
            case RECENT:
                TreeSet<BusURL> set2 = new TreeSet<BusURL>(BusURL.nameComparator);
                set2.addAll(manager.getRecent());
                setKeys(set2.toArray(new BusURL[set2.size()]));
                break;
        }
    }

}
