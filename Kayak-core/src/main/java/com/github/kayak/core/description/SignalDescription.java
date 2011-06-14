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
package com.github.kayak.core.description;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A SignalDescription contains the parameters and methods to extract
 * {@link Signal}s from a {@link Frame}.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class SignalDescription {

    public static enum Type {
        SIGNED, UNSIGNED, SINGLE, DOUBLE
    };

    private double slope;
    private double intercept;
    private int offset;
    private int length;
    private String unit;
    private String notes;
    private String name;
    private Type type;
    private HashMap<Integer, String> labels;
    private HashSet<String> consumer;
    private ByteOrder byteOrder;
    private MessageDescription message;

    public MessageDescription getMessage() {
        return message;
    }

    public HashSet<String> getConsumer() {
        return consumer;
    }

    public void setConsumer(HashSet<String> consumer) {
        this.consumer = consumer;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getIntercept() {
        return intercept;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    protected SignalDescription(MessageDescription message) {
        byteOrder = ByteOrder.LITTLE_ENDIAN;
        length = 1;
        offset = 0;
        unit = "";
        notes = "";
        intercept = 0;
        slope = 1;
        type = Type.UNSIGNED;
        this.message = message;
    }

    public Signal decodeData(byte[] data) {
        Signal signal = new Signal();
        signal.setUnit(unit);
        signal.setNotes(notes);

        /* read raw value */
        long rawValue = extractBits(data, offset, length, byteOrder);

        signal.setRawValue(rawValue);

        switch(type) {  
            case SIGNED:
                long signBit = (long) (1L << ((long) length - 1L));
                long signedRawValue = rawValue - ((rawValue & signBit) << 1);
                long signedValue = signedRawValue * (long) slope + (long) intercept;
                signal.setValue(Long.toString(signedValue));
                break;
            case UNSIGNED:
                long unsignedValue = rawValue * (long) slope + (long) intercept;
                signal.setValue(Long.toString(unsignedValue));
                break;
            case SINGLE:
                float floatValue = (float) rawValue * (float) slope + (float) intercept;
                signal.setValue(Float.toString(floatValue));
                break;
            case DOUBLE:
                double doubleValue = (double) rawValue * slope + intercept;
                signal.setValue(Double.toString(doubleValue));
                break;
        }

        return signal;
    }

    /**
     * This function extracts a given number of bits out of a byte array starting at a
     * given position. The result is returned as a long.
     * There are two issues with this approach. First we can not handle values that are
     * wider than 2^63bit (because long is signed). This is possibly rarely used but
     * nevertheless should be somehow fixed. Second the performance could be poor
     * because calculation is done bitwise.
     * @param data The byte array to work with
     * @param offset The first bit to be extracted
     * @param length The number of bits that have to be extracted
     * @param order The {@link ByteOrder} for the extraction
     * @return Long representation of the extracted bits
     */
    private long extractBits(byte[] data, int offset, int length, ByteOrder order) {
        long val = 0;

        if(order == ByteOrder.LITTLE_ENDIAN) {
            for (int i = 0; i < length; i++) {
                int bitNr = i + offset;
                val |= ((data[bitNr >> 3] >> (bitNr & 0x07)) & 1) << i;
            }
        } else {            
            for (int i = 0; i < length; i++) {
                int bitNr = offset + length - i -1;
                val |= ((data[bitNr >> 3] >> (7-(bitNr & 0x07))) & 1) << i;
            }
        }

        return val;
    }
}
