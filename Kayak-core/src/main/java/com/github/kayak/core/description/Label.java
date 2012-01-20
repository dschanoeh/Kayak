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

/**
 * A label maps specific signal raw values to a human readable label.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class Label {

    private long start;
    private long stop;
    private String label;

    public String getLabel() {
        return label;
    }

    public long getStart() {
        return start;
    }

    public long getStop() {
        return stop;
    }

    public Label(long value, String label) {
        start = stop = value;
        this.label = label;
    }

    public Label(long start, long stop, String label) {
        this.start = start;
        this.stop = stop;
        this.label = label;
    }

    public boolean isInRange(long rawValue) {
        if(rawValue >= start && rawValue <= stop)
            return true;
        return false;
    }


}
