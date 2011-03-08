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

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class FrameData {

    private int identifier;
    private byte[] data;
    private int[] frequency;
    private long timestamp;
    private long interval;

    public byte[] getData() {
        return data;
    }

    public int getIdentifier() {
        return identifier;
    }

    public long getInterval() {
        return interval;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int[] getFrequency() {
        return frequency;
    }

    public FrameData(Frame f) {
        this.identifier = f.getIdentifier();
        this.timestamp = f.getTimestamp();
        this.data = f.getData();
        this.frequency = new int[data.length];
        this.interval = 0;
    }

    public void updateWith(Frame frame) {
        interval = (interval * 24 + (frame.getTimestamp() - timestamp) * 8) / 32;

        byte[] newData = frame.getData();
        if(newData.length == data.length) {
            for(int i=0;i<data.length;i++) {
                if(data[i] != newData[i] && frequency[i] < 255)
                    frequency[i] += 2;
                if(frequency[i]>=1)
                    frequency[i]--;
            }
        } else {
            frequency = new int[data.length];
        }

        this.data = newData;
        this.timestamp = frame.getTimestamp();
    }

}
