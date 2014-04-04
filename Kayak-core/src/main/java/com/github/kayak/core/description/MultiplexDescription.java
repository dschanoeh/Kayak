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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description of a multiplex field in a message. Contains multiple signals
 * with different multiplex values.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class MultiplexDescription  {

    private final Map<Long, Set<SignalDescription>> signals = Collections.synchronizedMap(new HashMap<Long, Set<SignalDescription>>());

    private int length;
    private int offset;
    private ByteOrder byteOrder;
    private String name;
    private MessageDescription description;
    private SignalDescription.Type type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    
     public SignalDescription.Type getType() {
        return type;
    }

    public void setType(SignalDescription.Type type) {
        this.type = type;
    }

    /**
     * Returns all signals within the multiplex group regardless of their
     * mutliplex count value.
     * @return
     */
    public Set<SignalDescription> getAllSignalDescriptions() {
        synchronized(signals) {
            Set<Long> keys = signals.keySet();
            Set<SignalDescription> descriptions = new HashSet<SignalDescription>();

            for(long key : keys) {
                descriptions.addAll(signals.get(key));
            }

            return descriptions;
        }
    }

    protected MultiplexDescription(MessageDescription description) {
        this.description = description;
    }

    /**
     * Decode data according to the multiplex and signal definition. may return
     * zero, one or multiple signals.
     * @param data
     * @return
     */
    public Set<Signal> decodeData(byte[] data) throws DescriptionException {
        long rawValue = SignalDescription.extractBits(data, getOffset(), getLength(), getByteOrder());

        Set<SignalDescription> signalDescriptions = signals.get(rawValue);

        if(signalDescriptions == null)
            return null;

        Set<Signal> si = new HashSet<Signal>();
        for(SignalDescription s : signalDescriptions) {
            Signal signal = s.decodeData(data);
            signal.setMultiplexed(true);
            si.add(signal);
        }

        return si;
    }

    public SignalDescription createMultiplexedSignal(long count) {
        SignalDescription s = new SignalDescription(this, description, count);
        Set<SignalDescription> descriptions = signals.get(count);

        if(descriptions == null) {
            descriptions = new HashSet<SignalDescription>();
            signals.put(count, descriptions);
        }

        descriptions.add(s);
        return s;
    }
    
    public SignalDescription getMultiplexAsSignal() {
        SignalDescription s = new SignalDescription(description);
        
        s.setByteOrder(byteOrder);
        s.setIntercept(0);
        s.setLength(length);
        s.setName(name);
        s.setNotes("Multiplex");
        s.setSlope(1);
        s.setType(type);
        s.setUnit("1");
        
        return s;
    }

}
