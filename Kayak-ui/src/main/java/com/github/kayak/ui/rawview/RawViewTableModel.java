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

package com.github.kayak.ui.rawview;

import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameReceiver;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class RawViewTableModel extends AbstractTableModel implements FrameReceiver {

    private TreeMap<Integer, Frame> data;
    private final Set<Integer> refreshedRows = Collections.synchronizedSet(new HashSet<Integer>());
    private final Set<Integer> addedRows = Collections.synchronizedSet(new HashSet<Integer>());
    private Thread refreshThread;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            while (true) {
                if (!refreshedRows.isEmpty()) {
                    synchronized (refreshedRows) {
                        for (Integer row : refreshedRows) {
                            fireTableRowsUpdated(row, row);
                        }
                        refreshedRows.clear();
                    }
                }

                if (!addedRows.isEmpty()) {
                    synchronized (addedRows) {
                        for (Integer row : addedRows) {
                            fireTableRowsInserted(row, row);
                        }
                        addedRows.clear();
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }

    };

    public RawViewTableModel() {
        data = new TreeMap<Integer, Frame>();

        refreshThread = new Thread(refreshRunnable);
        refreshThread.start();
    }

    public void clear() {
        synchronized(this) {
            data.clear();
        }
    }

    @Override
    public int getRowCount() {
        synchronized(this) {
            return data.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    private int getRowForKey(int key) {
        Integer[] keys = data.keySet().toArray(new Integer[] {});

        for(int i=0;i<keys.length;i++)
            if(keys[i] == key)
                return i;

        return -1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        synchronized (this) {
            Integer[] keys = data.keySet().toArray(new Integer[]{});

            switch (columnIndex) {
                case 0:
                    long timestamp = data.get(keys[rowIndex]).getTimestamp();
                    java.util.Formatter formatter = new java.util.Formatter();
                    return formatter.format("%.3f",(double) timestamp/1000);
                case 1:
                    return "0x" + Integer.toHexString(data.get(keys[rowIndex]).getIdentifier());
                case 2:
                    return data.get(keys[rowIndex]).getData().length;
                case 3:
                    byte[] dat = data.get(keys[rowIndex]).getData();
                    String datString = com.github.kayak.core.Util.byteArrayToHexString(dat);
                    if (datString.length() % 2 != 0) {
                        datString = "0" + datString;
                    }

                    String res = "";
                    for (int i = 0; i < datString.length(); i += 2) {
                        res += datString.substring(i, i + 2);
                        if (i != datString.length()) {
                            res += " ";
                        }
                    }
                    return res;
                default:
                    return null;
            }
        }
    }

    @Override
    public void newFrame(Frame frame) {
        synchronized(this) {
            int row = getRowForKey(frame.getIdentifier());
            if (row != -1) {
                Frame old = data.get(frame.getIdentifier());
                data.remove(frame.getIdentifier());
                data.put(frame.getIdentifier(), frame);
                refreshedRows.add(row);
            } else {
                data.put(frame.getIdentifier(), frame);
                int newRow = getRowForKey(frame.getIdentifier());
                addedRows.add(newRow);
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Integer.class;
            case 3:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Timestamp";
            case 1:
                return "Identifier";
            case 2:
                return "DLC";
            case 3:
                return "Data";
            default:
                return null;
        }
    }

}
