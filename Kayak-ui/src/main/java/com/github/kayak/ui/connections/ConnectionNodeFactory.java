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

import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

public class ConnectionNodeFactory extends ChildFactory {

    private static enum Folder {
        DISCOVERY, FAVOURITES, RECENT;
    }

    public ConnectionNodeFactory() {
    }

    @Override
    protected boolean createKeys(List toPopulate) {
        toPopulate.add(Folder.DISCOVERY);
        toPopulate.add(Folder.FAVOURITES);
        toPopulate.add(Folder.RECENT);

        return true;
    }

    @Override
    protected Node[] createNodesForKey(Object key) {
        if(key instanceof Folder) {
            Folder folder = (Folder) key;
            switch(folder) {
                case DISCOVERY:
                    AbstractNode discoveryNode = new AbstractNode(new ConnectionChildrenFactory(BusURLNode.Type.DISCOVERY));
                    discoveryNode.setDisplayName("Auto discovery");
                    discoveryNode.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/devices/network-wireless.png");
                    return new Node[] { discoveryNode };
                case FAVOURITES:
                    AbstractNode favouritesFolderNode = new AbstractNode(new ConnectionChildrenFactory(BusURLNode.Type.FAVOURITE));
                    favouritesFolderNode.setDisplayName("Favourites");
                    favouritesFolderNode.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/actions/bookmark-new.png");
                    return new Node[] { favouritesFolderNode };
                case RECENT:
                    AbstractNode recentFolderNode = new AbstractNode(new ConnectionChildrenFactory(BusURLNode.Type.RECENT));
                    recentFolderNode.setDisplayName("Recent");
                    recentFolderNode.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/places/folder.png");
                    return new Node[] { recentFolderNode };
                default:
                    return null;
            }
        }

        return null;
    }

}
