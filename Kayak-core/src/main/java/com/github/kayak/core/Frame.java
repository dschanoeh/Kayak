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

package com.github.kayak.core;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * A frame is a atomic unit of data on a CAN bus. It contains the raw data and is
 * identified by the identifier. Optionally a timestamp can be added which
 * represents the time when the frame was received. The BusName indicates from
 * which bus this frame was received.
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class Frame {

    private byte[] data;
    private int identifier;
    private long timestamp;
    private Bus bus;

    public static final Pattern LogFileNotationPattern = Pattern.compile("\\([0-9]+\\.[0-9]{6}\\)[\\s]+[a-zA-Z0-9]{1,16}[\\s]+[A-Za-z0-9]{3,8}#[A-Fa-f0-9rR]+");

    public static class IdentifierComparator implements Comparator<Frame> {

        @Override
        public int compare(Frame f1, Frame f2) {
            if(f1.equals(f2) || f1.getIdentifier() == f2.getIdentifier())
                return 0;

            if(f1.getIdentifier() < f2.getIdentifier())
                return -1;

            return 1;
        }

    };

    public static class TimestampComparator implements Comparator<Frame> {

        @Override
        public int compare(Frame f1, Frame f2) {
            if(f1.equals(f2) || f1.getTimestamp() == f2.getTimestamp())
                return 0;

            if(f1.getTimestamp() < f2.getTimestamp())
                return -1;

            return 1;
        }
    };

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Frame() {

    }

    public Frame(int identifier, byte[] data) {
        this.identifier = identifier;
        this.data = data;
    }

    public Frame(int identifier, byte[] data, long timestamp) {
        this.identifier = identifier;
        this.data = data;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getLength() {
        return data.length;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        String s = "Frame [" + Integer.toHexString(identifier) + "] " + Util.byteArrayToHexString(data, true);
        return s;
    }

    public String toLogFileNotation() {
        StringBuilder sb = new StringBuilder(40);

        sb.append('(');
        sb.append(Long.toString(timestamp/1000000));
        sb.append('.');
        sb.append(String.format("%06d",timestamp%1000000));
        sb.append(") ");
        sb.append(bus.getName());
        sb.append(" ");
        if(isExtendedIdentifier()) {
            sb.append(String.format("%08x", identifier));
        } else {
            sb.append(String.format("%03x", identifier));
        }
        sb.append('#');
        for(byte b : data) {
            sb.append(String.format("%02x", b));
        }
        sb.append('\n');
        return sb.toString();
    }

    public static Frame fromLogFileNotation(String line) {
        if(!LogFileNotationPattern.matcher(line).matches())
            return null;

        int dotPos = -1;
        for(int i=1;i<line.length();i++) {
            if(line.charAt(i) == '.') {
                dotPos = i;
                break;
            }
        }

        if(dotPos == -1)
            return null;

        int bracketPos = -1;

        for(int i=dotPos;i<line.length();i++) {
            if(line.charAt(i) == ')') {
                bracketPos = i;
                break;
            }
        }

        if(bracketPos == -1)
            return null;

        long msecs = Long.parseLong(line.substring(1, dotPos)) * 1000000 + Long.parseLong(line.substring(dotPos+1, bracketPos));

        int idPos = -3;

        for(int i=bracketPos+1;i<line.length();i++) {
            switch(idPos) {
                case -3:
                    if(line.charAt(i) != ' ') {
                        idPos = -2;
                    }
                    break;
                case -2:
                    if(line.charAt(i) == ' ') {
                        idPos = -1;
                    }
                    break;
                case -1:
                    if(line.charAt(i) != ' ') {
                        idPos = i;
                    }
                    break;
                default:
                    break;
            }

            if(idPos >= 0)
                break;
        }

        if(idPos < 0)
            return null;

        int hashPos = -1;

        for(int i=idPos+1;i<line.length();i++) {
            if(line.charAt(i) == '#') {
                hashPos = i;
                break;
            }
        }

        int identifier = Integer.parseInt(line.substring(idPos, hashPos), 16);

        byte[] message = Util.hexStringToByteArray(line.substring(hashPos+1, line.length()));

        Frame frame = new Frame(identifier, message);
        frame.setTimestamp(msecs);

        return frame;
    }

    public boolean isExtendedIdentifier() {
        return (identifier > 2048);
    }
}
