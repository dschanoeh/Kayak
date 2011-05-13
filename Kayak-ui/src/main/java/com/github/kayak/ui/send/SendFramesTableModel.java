/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author dschanoeh
 */
public class SendFramesTableModel extends AbstractTableModel {
    
    private static final Logger logger = Logger.getLogger(SendFramesTableModel.class.getCanonicalName());
    
    private class TableRow {
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
    
    public void remove(int row) {
        rows.remove(row);
        fireTableRowsDeleted(row, row);
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
    
    public void add(Bus bus) {
        rows.add(new TableRow(bus));
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
                return "0x" + Integer.toHexString(rows.get(rowIndex).getId());
            case 2:
                return rows.get(rowIndex).getData().length;
            case 3:
                return Util.byteArrayToHexString(rows.get(rowIndex).getData());
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
                row.setLength(Integer.valueOf((String) aValue));
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
                    int interval = Integer.valueOf((String) aValue);
                    row.setInterval(interval);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Could not set new ID", ex);
                }
                return;
            case 6:
                Boolean b = Boolean.parseBoolean((String) aValue);
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
                return "ID";
            case 2:
                return "Length";
            case 3:
                return "Data";
            case 4:
                return "Send";
            case 5:
                return "Interval (ms)";
            case 6:
                return "Send interval";
            case 7:
                return "Note";
            default:
                return "";
        }
    }
    
}
