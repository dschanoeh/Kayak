/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.SignalDescription;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dschanoeh
 */
public class SignalTableModel extends AbstractTableModel implements MessageSignalDropAdapter.Receiver {

    private ArrayList<SignalDescription> signalsDescriptions = new ArrayList<SignalDescription>();

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        switch(columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
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
                return "Value";
            case 2:
                return "Message";
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
        return 3;
    }

    public void remove(int i) {
        signalsDescriptions.remove(i);
        fireTableRowsDeleted(i, i);
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SignalDescription signal = signalsDescriptions.get(rowIndex);

        switch(columnIndex) {
            case 0:
                return signal.getName();
            case 1:
                return "";
            case 2:
                return "";
            default:
                return null;
        }
    }

    public void addSignal(SignalDescription desc) {
        signalsDescriptions.add(desc);

        fireTableRowsInserted(signalsDescriptions.size()-1, signalsDescriptions.size()-1);
    }

    @Override
    public void dropped(SignalDescription signal) {
        addSignal(signal);
    }

    @Override
    public void dropped(MessageDescription message) {
        for(SignalDescription s : message.getSignals())
            addSignal(s);
    }
    
}
