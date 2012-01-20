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

package com.github.kayak.ui.statistics;

import com.github.kayak.core.StatisticsListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class StatisticsTableModel extends AbstractTableModel implements StatisticsListener {

    private int interval = 1000;

    private long lastRxBytes = 0;
    private long lastRxPackets = 0;
    private long lastTxBytes = 0;
    private long lastTxPackets = 0;
    private long rxBytesPerSecond = 0;
    private long rxPacketsPerSecond = 0;
    private long txBytesPerSecond = 0;
    private long txPacketsPerSecond = 0;

    public int getInterval() {
        return interval;
    }

    @Override
    public int getRowCount() {
        return 4;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "";
            case 1:
                return "Absolute";
            case 2:
                return "Per second";
        }
        return null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            switch(rowIndex) {
                case 0:
                    return "RX bytes";
                case 1:
                    return "RX packets";
                case 2:
                    return "TX bytes";
                case 3:
                    return "TX packets";
            }
        } else if(columnIndex == 1) {
            switch(rowIndex) {
                case 0:
                    return lastRxBytes;
                case 1:
                    return lastRxPackets;
                case 2:
                    return lastTxBytes;
                case 3:
                    return lastTxPackets;
            }
        } else if(columnIndex == 2) {
            switch(rowIndex) {
                case 0:
                    return rxBytesPerSecond;
                case 1:
                    return rxPacketsPerSecond;
                case 2:
                    return txBytesPerSecond;
                case 3:
                    return txPacketsPerSecond;
            }
        }

        return null;
    }

    @Override
    public void statisticsUpdated(long rxBytes, long rxPackets, long txBytes, long txPackets) {
            rxBytesPerSecond = (rxBytes - lastRxBytes) / (interval/1000);
            rxPacketsPerSecond = (rxPackets - lastRxPackets) / (interval/1000);
            txBytesPerSecond = (txBytes - lastTxBytes) / (interval/1000);
            txPacketsPerSecond = (txPackets - lastTxPackets) / (interval/1000);

            lastRxBytes = rxBytes;
            lastRxPackets = rxPackets;
            lastTxBytes = txBytes;
            lastTxPackets = txPackets;

            fireTableDataChanged();
    }

}
