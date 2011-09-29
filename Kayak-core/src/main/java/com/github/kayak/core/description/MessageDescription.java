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

import com.github.kayak.core.Frame;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class MessageDescription {

    public static final Comparator<MessageDescription> nameComparator = new Comparator<MessageDescription>() {

        @Override
        public int compare(MessageDescription o1, MessageDescription o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private int id;
    private int interval;
    private String name;
    private Node producer;
    private final Set<SignalDescription> signals = Collections.synchronizedSet(new HashSet<SignalDescription>());
    private final Set<MultiplexDescription> multiplexes = Collections.synchronizedSet(new HashSet<MultiplexDescription>());

    public Node getProducer() {
        return producer;
    }

    public void setProducer(Node producer) {
        this.producer = producer;
    }

    public int getId() {
        return id;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SignalDescription> getSignals() {
        return Collections.unmodifiableSet(signals);
    }

    public Set<MultiplexDescription> getMultiplexes() {
        return Collections.unmodifiableSet(multiplexes);
    }

    public void addMultiplex(MultiplexDescription m) {
        synchronized(multiplexes) {
            multiplexes.add(m);
        }
    }

    public void addSignal(SignalDescription s) {
        synchronized(signals) {
            signals.add(s);
        }
    }

    public MessageDescription(int id) {
        this.id = id;
    }

    public Message decodeFrame(Frame f) {

        Message m = new Message();
        m.setId(id);
        m.setInterval(interval);
        m.setName(name);
        if(producer!=null)
            m.setProducer(producer.getName());

        byte[] data = f.getData();
        HashSet<Signal> sig = new HashSet<Signal>();

        for(SignalDescription s : signals) {
            Signal signal = s.decodeData(data);
            if(s != null)
                sig.add(signal);
        }

        for(MultiplexDescription mul : multiplexes) {
            sig.addAll(mul.decodeData(data));
        }

        m.setSignals(sig);

        return m;
    }

    public SignalDescription createSignalDescription() {
        SignalDescription s = new SignalDescription(this);
        signals.add(s);
        return s;
    }

    public MultiplexDescription createMultiplexDescription() {
        MultiplexDescription m = new MultiplexDescription(this);
        multiplexes.add(m);
        return m;
    }

}
