/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging.input;

import com.github.kayak.core.Bus;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 *
 * @author dschanoeh
 */
public class BusDropTargetAdapter extends DropTargetAdapter {

    public static interface BusDropReceiver {

        void receive(Bus b, int number);

    };
    
    private int number;
    private BusDropReceiver receiver;

    public BusDropTargetAdapter(BusDropReceiver receiver, int number) {
        this.number = number;
        this.receiver = receiver;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (!dtde.isDataFlavorSupported(
                com.github.kayak.ui.projects.BusNode.DATA_FLAVOR)) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            com.github.kayak.ui.projects.BusNode n = (com.github.kayak.ui.projects.BusNode) dtde.getTransferable().
                    getTransferData(com.github.kayak.ui.projects.BusNode.DATA_FLAVOR);

            receiver.receive(n.getBus(), number);

        } catch (Exception e) {
            e.printStackTrace();
            dtde.rejectDrop();
        }
    }
}
