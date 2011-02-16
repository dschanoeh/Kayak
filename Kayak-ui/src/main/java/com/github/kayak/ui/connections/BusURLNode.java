/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.connections;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dsi9mjn
 */
public class BusURLNode extends AbstractNode {
    public static enum Type {
        RECENT, FAVOURITE, DISCOVERY
    }

    private static Type type;

    public static Type getType() {
        return type;
    }

    private class BookmarkConnectionAction extends AbstractAction {


        public BookmarkConnectionAction() {
            putValue(NAME, "Bookmark");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BusURL beacon = getLookup().lookup(BusURL.class);
            BusBeaconDiscoveryFactory discoveryFactory = Lookup.getDefault().lookup(BusBeaconDiscoveryFactory.class);
            if(discoveryFactory != null)
                JOptionPane.showMessageDialog(null, "Hello from " + beacon);
        }

    }

    public BusURLNode(BusURL beacon, Type type) {
        super(Children.LEAF, Lookups.fixed(beacon));
        setDisplayName(beacon.toString());
        this.type = type;
    }

    @Override
    public Action[] getActions(boolean popup) {
        return new Action[] { new BookmarkConnectionAction() };
    }
}
