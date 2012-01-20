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
package com.github.kayak.mapview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.description.SignalDescription;
import com.github.kayak.ui.messageview.SignalDescriptionNode;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
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
