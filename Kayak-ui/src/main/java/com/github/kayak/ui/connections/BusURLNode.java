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
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author dsi9mjn
 */
public class BusURLNode extends AbstractNode {
    public static enum Type {
        RECENT, FAVOURITE, DISCOVERY, CONNECTED
    }

    private static Type type;
    private BusURL url;
    private ConnectionManager manager = ConnectionManager.getGlobalConnectionManager();

    public static Type getType() {
        return type;
    }

    @Override
    public Transferable drag() throws IOException {
        return url;
    }


    private class BookmarkConnectionAction extends AbstractAction {

        public BookmarkConnectionAction() {
            putValue(NAME, "Bookmark");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(type == BusURLNode.Type.DISCOVERY || type == BusURLNode.Type.RECENT) {
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

    public BusURLNode(BusURL url, Type type) {
        super(Children.LEAF);
        this.url = url;
        setDisplayName(url.toString());
        this.type = type;
    }

    @Override
    public Action[] getActions(boolean popup) { 
        return new Action[] { new BookmarkConnectionAction(), new DeleteConnectionAction() };
    }
}
