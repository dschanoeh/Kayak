/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameReceiver;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.description.Message;
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.Signal;
import com.github.kayak.core.description.SignalDescription;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dschanoeh
 */
public class SignalTableModel extends AbstractTableModel implements MessageSignalDropAdapter.Receiver {

    private ArrayList<SignalDescription> signalsDescriptions = new ArrayList<SignalDescription>();
    private HashMap<Bus, Subscription> subscriptions = new HashMap<Bus, Subscription>();
    private HashMap<SignalDescription, Signal> signals = new HashMap<SignalDescription, Signal>();

    private FrameReceiver receiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame frame) {
            Bus bus = frame.getBus();
            
            Message m = bus.getDescription().decodeFrame(frame);
            HashSet<Signal> frameSignals = m.getSignals();

            for(Signal s : frameSignals) {
               if(signalsDescriptions.contains(s.getDescription())) {
                    signals.put(s.getDescription(), s);             
               }
            }
        }
    };

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
                return Long.class;
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
                return "Value";
            case 2:
                return "Unit";
            case 3:
                return "Message";
            case 4:
                return "Raw value";
            default:
                return "";
        }
    }

    @Override
    public int getRowCount() {
        return signalsDescriptions.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    public void remove(int i) {
        signalsDescriptions.remove(i);
        fireTableRowsDeleted(i, i);
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SignalDescription signalDesc = signalsDescriptions.get(rowIndex);
        Signal signal = signals.get(signalDesc);

        switch(columnIndex) {
            case 0:
                return signalDesc.getName();
            case 1:
                if(signal != null) {
                    return signal.getValue();
                } else {
                    return "";
                }
            case 2:
                return signalDesc.getUnit();
            case 3:
                return signalDesc.getMessage().getName();
            case 4:
                if(signal != null)
                    return signal.getRawValue();
                else
                    return 0;
            default:
                return null;
        }
    }

    public void addSignal(SignalDescription desc, Bus bus) {
        if(!signalsDescriptions.contains(desc)) {
            signalsDescriptions.add(desc);
            fireTableRowsInserted(signalsDescriptions.size()-1, signalsDescriptions.size()-1);
            
            Subscription s;
            if(subscriptions.containsKey(bus)) {
                s = subscriptions.get(bus);
            } else {
                s = new Subscription(receiver, bus);
                subscriptions.put(bus, s);
            }

            s.subscribe(desc.getMessage().getId());
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
