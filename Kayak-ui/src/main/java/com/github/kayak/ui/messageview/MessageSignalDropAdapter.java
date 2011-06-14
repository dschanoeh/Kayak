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
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.SignalDescription;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
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
