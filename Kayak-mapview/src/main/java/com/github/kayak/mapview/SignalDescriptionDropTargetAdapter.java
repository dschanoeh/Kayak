/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.mapview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.description.SignalDescription;
import com.github.kayak.ui.messageview.SignalDescriptionNode;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 *
 * @author dschanoeh
 */
public class SignalDescriptionDropTargetAdapter extends DropTargetAdapter {

    public static interface SignalDescriptionDropReceiver {

        void receive(Bus b, SignalDescription desc);

    };
    
    private SignalDescriptionDropReceiver receiver;

    public SignalDescriptionDropTargetAdapter(SignalDescriptionDropReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (!dtde.isDataFlavorSupported(
                SignalDescriptionNode.SIGNAL_DATA_FLAVOR)) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            Bus b = (Bus) dtde.getTransferable().getTransferData(SignalDescriptionNode.BUS_DATA_FLAVOR);
            SignalDescription d = (SignalDescription) dtde.getTransferable().getTransferData(SignalDescriptionNode.SIGNAL_DATA_FLAVOR);

            receiver.receive(b, d);

        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }
}
