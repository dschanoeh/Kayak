/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.connections;

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
                    /*Children.Keys<ArrayList<BusURL>> discoveryChildren = new Children.Keys<ArrayList<BusURL>>() {

                        @Override
                        protected Node[] createNodes(ArrayList<BusURL> key) {
                            ArrayList<Node> nodes = new ArrayList<Node>();
                            for(BusURL busURL : key) {
                                nodes.add(new BusURLNode(busURL, BusURLNode.Type.RECENT));
                            }
                            return nodes.toArray(new Node[0]);
                        }
                    };*/
                    Children.Array discoveryChildren = new Children.Array();
                    for(BusURL url : manager.getAutoDiscovery()) {
                        discoveryChildren.add(createNodesForKey(url));
                    }

                    AbstractNode discoveryFolderNode = new AbstractNode(discoveryChildren);
                    discoveryFolderNode.setDisplayName("Auto discovery");   
                    return new Node[] { discoveryFolderNode };
                case FAVOURITES:
                    AbstractNode favouritesFolderNode = new AbstractNode(Children.LEAF);
                    favouritesFolderNode.setDisplayName("Favourites");
                    return new Node[] { favouritesFolderNode };
                case RECENT:
                    AbstractNode recentFolderNode = new AbstractNode(Children.LEAF);
                    recentFolderNode.setDisplayName("Recent");
                    return new Node[] { recentFolderNode };
                default:
                    return null;
            }
        } else if(key instanceof BusURL) {
            BusURLNode node = new BusURLNode((BusURL) key, BusURLNode.Type.RECENT);
            return new Node[] { node };
        }
        return null;
    }
}
