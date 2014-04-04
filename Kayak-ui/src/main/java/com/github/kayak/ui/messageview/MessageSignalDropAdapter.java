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

import com.github.kayak.core.Bus;
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.MultiplexDescription;
import com.github.kayak.core.description.SignalDescription;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class MessageSignalDropAdapter extends DropTargetAdapter{

    public static interface Receiver {
        
        public void dropped(SignalDescription signal, Bus bus);

        public void dropped(MessageDescription message, Bus bus);
        
        public void dropped(MultiplexDescription mux, Bus bus);
        
    };

    private Receiver receiver;

    public MessageSignalDropAdapter(Receiver r) {
        this.receiver = r;        
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (!dtde.isDataFlavorSupported(MessageDescriptionNode.BUS_DATA_FLAVOR) &&
                !dtde.isDataFlavorSupported(SignalDescriptionNode.BUS_DATA_FLAVOR)) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        Transferable transferable = dtde.getTransferable();
        
        if(transferable.isDataFlavorSupported(MessageDescriptionNode.MESSAGE_DATA_FLAVOR)) {
            try {
                MessageDescription desc = (MessageDescription) transferable.getTransferData(MessageDescriptionNode.MESSAGE_DATA_FLAVOR);
                Bus bus = (Bus) transferable.getTransferData(MessageDescriptionNode.BUS_DATA_FLAVOR);
                    receiver.dropped(desc, bus);
                    return;
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(transferable.isDataFlavorSupported(SignalDescriptionNode.SIGNAL_DATA_FLAVOR)) {
            try {
                SignalDescription desc = (SignalDescription) transferable.getTransferData(SignalDescriptionNode.SIGNAL_DATA_FLAVOR);
                Bus bus = (Bus) transferable.getTransferData(SignalDescriptionNode.BUS_DATA_FLAVOR);
                    receiver.dropped(desc, bus);
                    return;
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(transferable.isDataFlavorSupported(MultiplexDescriptionNode.MULTIPLEX_DATA_FLAVOR)) {
            try {
                MultiplexDescription desc = (MultiplexDescription) transferable.getTransferData(MultiplexDescriptionNode.MULTIPLEX_DATA_FLAVOR);
                Bus bus = (Bus) transferable.getTransferData(MultiplexDescriptionNode.BUS_DATA_FLAVOR);
                    receiver.dropped(desc, bus);
                    return;
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
