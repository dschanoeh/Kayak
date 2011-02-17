/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.connections;

import com.github.kayak.ui.busses.BusNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author dsi9mjn
 */
public class ConnectionNodeFactory extends ChildFactory implements ConnectionListener {
    private ConnectionManager manager;


    @Override
    public void connectionsChanged() {
        refresh(false);
    }

    private static enum Folder {
        DISCOVERY, FAVOURITES, RECENT;
    }

    public ConnectionNodeFactory(ConnectionManager manager) {
        this.manager = manager;
        manager.addConnectionListener(this);
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
                    AbstractNode discoveryNode = new AbstractNode(new ConnectionChildrenFactory(manager, BusURLNode.Type.DISCOVERY));
                    discoveryNode.setDisplayName("Auto discovery");
                    discoveryNode.setIconBaseWithExtension("com/github/kayak/ui/connections/network-wireless.png");
                    return new Node[] { discoveryNode };
                case FAVOURITES:
                    AbstractNode favouritesFolderNode = new AbstractNode(new ConnectionChildrenFactory(manager, BusURLNode.Type.FAVOURITE));
                    favouritesFolderNode.setDisplayName("Favourites");
                    favouritesFolderNode.setIconBaseWithExtension("com/github/kayak/ui/connections/bookmark-new.png");
                    return new Node[] { favouritesFolderNode };
                case RECENT:
                    AbstractNode recentFolderNode = new AbstractNode(new ConnectionChildrenFactory(manager, BusURLNode.Type.RECENT));
                    recentFolderNode.setDisplayName("Recent");
                    recentFolderNode.setIconBaseWithExtension("com/github/kayak/ui/connections/folder.png");
                    return new Node[] { recentFolderNode };
                default:
                    return null;
            }
        } else if(key instanceof BusURL) {
            
        }
        return null;
    }

}
