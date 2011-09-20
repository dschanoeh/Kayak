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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class Message {

    Set<Signal> signals;
    private String name;
    private String producer;
    private int id;
    private int interval;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Set<Signal> getSignals() {
        return Collections.unmodifiableSet(signals);
    }

    public void setSignals(Set<Signal> signals) {
        this.signals = signals;
    }

    public Message() {
        signals = new HashSet<Signal>();
    }

}
