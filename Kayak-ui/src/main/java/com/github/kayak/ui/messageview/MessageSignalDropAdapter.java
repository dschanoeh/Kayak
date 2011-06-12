/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.SignalDescription;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;

/**
 *
 * @author dschanoeh
 */
public class MessageSignalDropAdapter extends DropTargetAdapter{

    public static interface Receiver {
        
        public void dropped(SignalDescription signal);

        public void dropped(MessageDescription message);
        
    };

    private Receiver receiver;

    public MessageSignalDropAdapter(Receiver r) {
        this.receiver = r;        
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (!dtde.isDataFlavorSupported(MessageDescriptionNode.DATA_FLAVOR) &&
                !dtde.isDataFlavorSupported(SignalDescriptionNode.DATA_FLAVOR)) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            try {
                MessageDescription n = (MessageDescription) dtde.getTransferable().getTransferData(MessageDescriptionNode.DATA_FLAVOR);
                if(n != null) {
                    receiver.dropped(n);
                    return;
                }

            } catch (UnsupportedFlavorException ex) {}

            try {
                SignalDescription n = (SignalDescription) dtde.getTransferable().getTransferData(SignalDescriptionNode.DATA_FLAVOR);
                if(n != null) {
                    receiver.dropped(n);
                    return;
                }

            } catch (UnsupportedFlavorException ex) {}
            
        } catch (IOException ex) {
            dtde.rejectDrop();
        }
    }
    
}
