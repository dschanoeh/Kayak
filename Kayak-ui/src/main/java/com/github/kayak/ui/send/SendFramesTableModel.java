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
package com.github.kayak.ui.send;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Frame;
import com.github.kayak.core.Util;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class SendFramesTableModel extends AbstractTableModel {

    private static final Logger logger = Logger.getLogger(SendFramesTableModel.class.getCanonicalName());

    public static class TableRow {
        private int id = 0;
        private int interval = 100;
        private byte[] data = new byte[] { 0x00 };
        private boolean sending;
        private Bus bus;
        private String note = "";
        private Thread thread;

        private Runnable runnable = new Runnable() {

            @Override
            public void run() {

                while(true) {
                    Frame f = new Frame(id, data);
                    bus.sendFrame(f);
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        };

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Bus getBus() {
            return bus;
        }

        public void setLength(int i) {
            byte[] newData = new byte[i];

            for(int j=0;j<newData.length && j<data.length;j++) {
                newData[j] = data[j];
            }

            data = newData;
        }

        public TableRow(Bus bus) {
            this.bus = bus;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }


        public void sendSingle() {
            logger.log(Level.INFO, "sending frame");
            Frame frame = new Frame(id, data);
            bus.sendFrame(frame);
        }

        public boolean isSending() {
            return sending;
        }

        public void setSending(boolean val) {
            if(val && !sending) {
                thread = new Thread(runnable);
                thread.setName("Frame send task");
                thread.start();
                sending = true;
            } else if(!val && sending) {
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    logger.log(Level.WARNING, "Interrupted while stopping send task", ex);
                }
                sending = false;
            }
        }
    }

    private ArrayList<TableRow> rows = new ArrayList<TableRow>();

    public void remove(int i) {
        try {
            if(rows.size() <= i || i < 0)
                return;

            TableRow row = rows.get(i);

            if(row.isSending())
                row.setSending(false);

            rows.remove(row);
            fireTableRowsDeleted(i, i);
        } catch(Exception ex) {
            logger.log(Level.WARNING, "Could not delete row.", ex);
        }

    }

    public void send(int row) {
        rows.get(row).sendSingle();
        fireTableRowsUpdated(row, row);
    }

    public void toggleSendInterval(int i) {
        TableRow row = rows.get(i);

        if(row.isSending())
            row.setSending(false);
        else
            row.setSending(true);

        fireTableRowsUpdated(i, i);
    }

    public TableRow getRow(int index) {
        return rows.get(index);
    }

    public void add(Bus bus) {
        rows.add(new TableRow(bus));
        fireTableRowsInserted(rows.size(), rows.size());
    }

    public void add(Bus bus, int id, int interval, byte[] data, String note) {
        TableRow row = new TableRow(bus);
        row.setData(data);
        row.setId(id);
        row.setInterval(interval);
        row.setNote(note);
        rows.add(row);
        fireTableRowsInserted(rows.size(), rows.size());
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 8;
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
            case 4:
                return String.class;
            case 5:
                return Integer.class;
            case 6:
                return Boolean.class;
            case 7:
                return String.class;
        }

        return super.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if(columnIndex != 0)
            return true;
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0:
                return rows.get(rowIndex).getBus().getName();
            case 1:
                return Integer.toHexString(rows.get(rowIndex).getId());
            case 2:
                return rows.get(rowIndex).getData().length;
            case 3:
                return Util.byteArrayToHexString(rows.get(rowIndex).getData(), false);
            case 4:
                return "Send";
            case 5:
                return rows.get(rowIndex).getInterval();
            case 6:
                return rows.get(rowIndex).isSending();
            case 7:
                return rows.get(rowIndex).getNote();
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        TableRow row = rows.get(rowIndex);

        switch(columnIndex) {
            case 1:
                try {
                    int id = Integer.valueOf(((String) aValue).substring(2), 16);
                    row.setId(id);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Could not set new ID", ex);
                }
                return;
            case 2:
                row.setLength((Integer) aValue);
                return;
            case 3:
                try {
                    byte[] newData = Util.hexStringToByteArray((String) aValue);
                    if(newData.length == row.getData().length) {
                        row.setData(newData);
                    }
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Could not set new row data", ex);
                }

                return;
            case 5:
                try {
                    int interval = (Integer) aValue;
                    row.setInterval(interval);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Could not set new ID", ex);
                }
                return;
            case 6:
                Boolean b = (Boolean) aValue;
                row.setSending(b);
                return;
            case 7:
                row.setNote((String) aValue);
                return;

            default:
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Bus";
            case 1:
                return "ID [hex]";
            case 2:
                return "Length";
            case 3:
                return "Data";
            case 4:
                return "Send";
            case 5:
                return "Interval [ms]";
            case 6:
                return "Send interval";
            case 7:
                return "Note";
            default:
                return "";
        }
    }

}
