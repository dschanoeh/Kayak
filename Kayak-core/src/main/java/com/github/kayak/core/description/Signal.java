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

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 * A signal is a single data value that can be extracted out of a {@link Frame}.
 * The parameters for the extraction are defined in the
 * {@link SignalDescription} which also contains the methods to generate
 * Signals.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class Signal {

    public static final Comparator<Signal> nameComparator = new Comparator<Signal>() {

        @Override
        public int compare(Signal o1, Signal o2) {
            return o1.getDescription().getName().compareTo(o2.getDescription().getName());
        }
    };

    private static final DecimalFormat readableFormat = new DecimalFormat("0.00");

    private long rawValue;
    private String unit;
    private double value;
    private Set<String> labels;
    private String notes;
    private SignalDescription description;
    private boolean multiplexed;

    public boolean isMultiplexed() {
        return multiplexed;
    }

    public void setMultiplexed(boolean multiplexed) {
        this.multiplexed = multiplexed;
    }

    public SignalDescription getDescription() {
        return description;
    }

    public void setDescription(SignalDescription description) {
        this.description = description;
    }

    public Set<String> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public long getRawValue() {
        return rawValue;
    }

    public void setRawValue(long rawValue) {
        this.rawValue = rawValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getExactString() {
        return Double.toString(value);
    }

    public String getReadableString() {
        if(labels != null && !labels.isEmpty()) {
            if(labels.size() == 1) {
                return labels.iterator().next();
            } else {
                StringBuilder sb = new StringBuilder();
                for(String label : labels) {
                    sb.append(label);
                    sb.append(", ");
                }
                sb.setLength(sb.length()-2);
                return sb.toString();
            }
        }
        return readableFormat.format(value);
    }

    public String getIntegerString() {
        return Long.toString(Math.round(value));
    }

    public Signal() {

    }
}
