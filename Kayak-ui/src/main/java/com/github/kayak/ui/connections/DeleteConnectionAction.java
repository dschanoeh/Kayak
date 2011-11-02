/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.connections;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionRegistration(displayName="Delete connection...", iconBase="org/tango-project/tango-icon-theme/16x16/actions/edit-delete.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="Connections", id="com.github.kayak.ui.connections.DeleteConnectionAction")
@ActionReferences(value = {
    @ActionReference(path="Menu/Connections", position=40)})
public final class DeleteConnectionAction extends AbstractAction {

    private final BusURLNode context;

    public DeleteConnectionAction(BusURLNode context) {
        this.context = context;

        putValue(NAME, "Delete");
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if(context.getType() == BusURLNode.Type.FAVOURITE) {
            ConnectionManager.getGlobalConnectionManager().removeFavourite(context.getURL());
        } else if(context.getType() == BusURLNode.Type.RECENT) {
            ConnectionManager.getGlobalConnectionManager().removeRecent(context.getURL());
        }
    }
}
