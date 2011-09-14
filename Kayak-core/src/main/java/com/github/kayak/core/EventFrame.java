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

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class EventFrame extends Frame {

    private String message;

    public String getMessage() {
        return message;
    }

    public EventFrame(String message) {
        this.message = message;
    }

    @Override
    public byte[] getData() {
        return new byte[]{};
    }

    @Override
    public int getIdentifier() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public void setData(byte[] data) {

    }

    @Override
    public String toLogFileNotation() {
        long timestamp = getTimestamp();
        String busName = getBus().getName();
        StringBuilder sb = new StringBuilder(40);

        sb.append("EVENT (");
        sb.append(Long.toString(timestamp/1000));
        sb.append('.');
        sb.append(String.format("%03d",timestamp%1000));
        sb.append("000) ");
        if(busName != null) {
            sb.append(busName);
            sb.append(" ");
        }
        sb.append("\"");
        sb.append(message);
        sb.append("\"\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Event: " + message;
    }

}
