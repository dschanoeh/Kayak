/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.connections;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public final class DeleteConnectionAction extends AbstractAction implements ActionListener {

    private final BusURLNode context;

    public DeleteConnectionAction(BusURLNode context) {
        this.context = context;

        putValue(NAME, "Delete");
    }

    public void actionPerformed(ActionEvent ev) {
        if(context.getType() == BusURLNode.Type.FAVOURITE) {
            ConnectionManager.getGlobalConnectionManager().removeFavourite(context.getURL());
        } else if(context.getType() == BusURLNode.Type.RECENT) {
            ConnectionManager.getGlobalConnectionManager().removeRecent(context.getURL());
        }
    }
}
