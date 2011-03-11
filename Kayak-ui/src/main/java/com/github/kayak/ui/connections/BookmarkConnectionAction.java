/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.connections;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public final class BookmarkConnectionAction extends AbstractAction implements ActionListener {

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
