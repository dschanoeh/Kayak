/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.connections;

import com.github.kayak.core.BusURL;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionRegistration(displayName="Bookmark connection...", iconBase="org/tango-project/tango-icon-theme/16x16/actions/bookmark-new.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="Connections", id="com.github.kayak.ui.connections.BookmarkConnectionAction")
@ActionReferences(value = {
    @ActionReference(path="Menu/Connections", position=30)})
public final class BookmarkConnectionAction extends AbstractAction {

    private final BusURL context;

    public BookmarkConnectionAction(BusURL context) {
        this.context = context;

        putValue(NAME, "Bookmark");
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ConnectionManager.getGlobalConnectionManager().addFavourite(context);
    }
}
