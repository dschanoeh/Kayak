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
    private BusURL url;
    private ConnectionManager manager;

    public static Type getType() {
        return type;
    }

    private class BookmarkConnectionAction extends AbstractAction {

        public BookmarkConnectionAction() {
            putValue(NAME, "Bookmark");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(type != BusURLNode.Type.FAVOURITE) {
                manager.addFavourite(url);
            }
        }
    }

    private class DeleteConnectionAction extends AbstractAction {

        public DeleteConnectionAction() {
            putValue(NAME, "Delete");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(type == BusURLNode.Type.FAVOURITE) {
                manager.removeFavourite(url);
            } else if(type == BusURLNode.Type.RECENT) {
                manager.removeRecent(url);
            }
        }
    }

    public BusURLNode(BusURL url, Type type, ConnectionManager manager) {
        super(Children.LEAF);
        this.url = url;
        setDisplayName(url.toString());
        this.type = type;
        this.manager = manager;
    }

    @Override
    public Action[] getActions(boolean popup) {
        if(type == BusURLNode.Type.FAVOURITE) {
            return new Action[] { new BookmarkConnectionAction(), new DeleteConnectionAction() };
        } else if(type == BusURLNode.Type.DISCOVERY) {
            return new Action[] { new BookmarkConnectionAction() };
        } else {
            return new Action[] { new BookmarkConnectionAction(), new DeleteConnectionAction() };
        }
    }
}
