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

    private TreeMap<Integer, FrameData> data;
    private final Set<Integer> refreshedRows = Collections.synchronizedSet(new HashSet<Integer>());
    private final Set<Integer> addedRows = Collections.synchronizedSet(new HashSet<Integer>());
    private Thread refreshThread;
    private boolean colorize = false;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            while (true) {
                if (!refreshedRows.isEmpty()) {
                    Integer[] rows;

                    synchronized (refreshedRows) {
                        rows = refreshedRows.toArray(new Integer[refreshedRows.size()]);
                    }

                    java.util.Arrays.sort(rows);

                    for (int i = 0; i < rows.length; i++) {
                        int sequenceLength = 0;
                        for (; sequenceLength < rows.length - i - 1; sequenceLength++) {
                            if (rows[i + sequenceLength + 1] != rows[i + 1] + sequenceLength) {
                                break;
                            }
                        }
                        fireTableRowsUpdated(rows[i], rows[i + sequenceLength]);
                        i += sequenceLength;
                    }
                    
                    synchronized (refreshedRows) {
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
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }

    };

    public boolean isColorized() {
        return colorize;
    }

    public void setColorized(boolean colorize) {
        this.colorize = colorize;
    }

    public RawViewTableModel() {
        data = new TreeMap<Integer, FrameData>();

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
        return 5;
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
                    return String.format("%.3f",(double) timestamp/1000);
                case 1:
                    return data.get(keys[rowIndex]).getInterval();
                case 2:
                    return "0x" + Integer.toHexString(data.get(keys[rowIndex]).getIdentifier());
                case 3:
                    return data.get(keys[rowIndex]).getData().length;
                case 4:
                    FrameData frameData = data.get(keys[rowIndex]);
                    byte[] dat = frameData.getData();
                    int[] frequency = frameData.getFrequency();

                    String datString = com.github.kayak.core.Util.byteArrayToHexString(dat);
                    if (datString.length() % 2 != 0) {
                        datString = "0" + datString;
                    }

                    if(colorize) {
                        String res = "<html>";
                        for (int i = 0; i < datString.length(); i += 2) {
                            res += "<font color=\"#";
                            String s = Integer.toHexString(frequency[i/2]);
                            if(s.length() == 1)
                                s = "0" + s;
                            res += s + "0000\">";

                            res += datString.substring(i, i + 2);
                            if (i != datString.length()) {
                                res += " ";
                            }
                            res += "</font>";
                        }
                        res += "</html>";
                        return res;
                    } else {
                        String res = "";
                        for (int i = 0; i < datString.length(); i += 2) {
                            res += datString.substring(i, i + 2);
                            if (i != datString.length()) {
                                res += " ";
                            }
                        }
                        return res;
                    }
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
                FrameData old = data.get(frame.getIdentifier());
                old.updateWith(frame);
                refreshedRows.add(row);
            } else {
                data.put(frame.getIdentifier(), new FrameData(frame));
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
                return Integer.class;
            case 2:
                return String.class;
            case 3:
                return Integer.class;
            case 4:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Timestamp [s]";
            case 1:
                return "Interval [ms]";
            case 2:
                return "Identifier [hex]";
            case 3:
                return "DLC";
            case 4:
                return "Data [hex]";
            default:
                return null;
        }
    }

}
