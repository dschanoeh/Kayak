/**
 *      This file is part of Kayak.
 *
 *      Kayak is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Kayak is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameListener;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.description.Message;
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.Signal;
import com.github.kayak.core.description.SignalDescription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class SignalTableModel extends AbstractTableModel implements MessageSignalDropAdapter.Receiver {

    public static enum Presentation {
        READABLE("Readable"),
        EXACT("Exact"),
        INTEGER("Integer");

        private final String _displayName;

        /** constructor */
        Presentation(final String displayName) {
            _displayName = displayName;
        }

        /** overrides method toString() in java.lang.Enum class */
        @Override
        public String toString(){
            return _displayName;
        }
    };

    private Presentation activePresentation = Presentation.READABLE;
    private Map<Bus, Subscription> subscriptions = new HashMap<Bus, Subscription>();
    private final ArrayList<SignalTableEntry> entries = new ArrayList<SignalTableEntry>();
    private Thread refreshThread;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            while(true) {
                int i=0;
                synchronized(entries) {
                    for(SignalTableEntry e : entries) {
                        if(e.isRefresh()) {
                            fireTableRowsUpdated(i, i);
                            e.setRefresh(false);
                        }
                        i++;
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    };

    private FrameListener receiver = new FrameListener() {

        @Override
        public void newFrame(Frame frame) {
            Bus bus = frame.getBus();

            Message m = bus.getDescription().decodeFrame(frame);
            Set<Signal> frameSignals = m.getSignals();

            synchronized(entries) {
                for(Signal s : frameSignals) {
                    for(SignalTableEntry entry : entries) {
                        if(s.getDescription().equals(entry.getDescription())) {
                            entry.setSignal(s);
                            entry.setRefresh(true);
                        }
                    }
                }
            }
        }
    };

    public Presentation getActivePresentation() {
        return activePresentation;
    }

    public void setActivePresentation(Presentation activePresentation) {
        this.activePresentation = activePresentation;
    }

    public SignalTableModel() {
        refreshThread = new Thread(refreshRunnable);
        refreshThread.start();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        switch(columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
            case 4:
                return String.class;
            default:
                return String.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Name";
            case 1:
                return "Value [dec]";
            case 2:
                return "Unit";
            case 3:
                return "Message";
            case 4:
                return "Raw value [hex]";
            default:
                return "";
        }
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    public void remove(int i) {
        synchronized(entries) {
            entries.remove(i);
            fireTableRowsDeleted(i, i);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SignalTableEntry entry = entries.get(rowIndex);
        Signal signal = entry.getSignal();

        switch(columnIndex) {
            case 0:
                return entry.getDescription().getName();
            case 1:
                if(signal != null) {
                    switch(activePresentation) {
                        case EXACT:
                            return signal.getExactString();
                        case INTEGER:
                            return signal.getIntegerString();
                        case READABLE:
                            return signal.getReadableString();
                    }
                } else {
                    return "";
                }
            case 2:
                return entry.getDescription().getUnit();
            case 3:
                MessageDescription message = entry.getDescription().getMessageDescription();
                return message.getName() + " (0x" + Integer.toHexString(message.getId()) + ")";
            case 4:
                if(signal != null)
                    return Long.toHexString(signal.getRawValue());
                else
                    return "0";
            default:
                return null;
        }
    }

    public void addSignal(SignalDescription desc, Bus bus) {
        boolean found = false;

        for(SignalTableEntry entry : entries) {
            if(entry.getBus() == bus && entry.getDescription() == desc) {
                found = true;
                break;
            }
        }

        if(!found) {
            SignalTableEntry entry = new SignalTableEntry();
            entry.setDescription(desc);
            entry.setBus(bus);
            synchronized(entries) {
                entries.add(entry);
            }
            fireTableRowsInserted(entries.size()-1, entries.size()-1);

            Subscription s;
            if(subscriptions.containsKey(bus)) {
                s = subscriptions.get(bus);
            } else {
                s = new Subscription(receiver, bus);
                subscriptions.put(bus, s);
            }

            s.subscribe(desc.getMessageDescription().getId());
        }
    }

    @Override
    public void dropped(SignalDescription signal, Bus bus) {
        addSignal(signal, bus);
    }

    @Override
    public void dropped(MessageDescription message, Bus bus) {
        for(SignalDescription s : message.getSignals())
            addSignal(s, bus);
    }

}
