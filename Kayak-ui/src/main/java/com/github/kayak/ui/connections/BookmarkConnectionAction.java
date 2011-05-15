/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.connections;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

@ActionRegistration(displayName="Bookmark connection...", iconBase="org/freedesktop/tango/16x16/actions/bookmark-new.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="Connections", id="com.github.kayak.ui.connections.BookmarkConnectionAction")
public final class BookmarkConnectionAction extends AbstractAction {

    private final BusURLNode context;

    public BookmarkConnectionAction(BusURLNode context) {
        this.context = context;

        putValue(NAME, "Bookmark");
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if(context.getType() != BusURLNode.Type.FAVOURITE) {
            ConnectionManager.getGlobalConnectionManager().addFavourite(context.getURL());
        }
    }
}
