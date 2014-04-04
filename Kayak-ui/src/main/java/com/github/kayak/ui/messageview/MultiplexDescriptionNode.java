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
package com.github.kayak.ui.messageview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.description.MultiplexDescription;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteOrder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class MultiplexDescriptionNode extends AbstractNode implements Transferable {

    public static final DataFlavor MULTIPLEX_DATA_FLAVOR = new DataFlavor(MultiplexDescription.class, "MultiplexDescription");
    public static final DataFlavor BUS_DATA_FLAVOR = new DataFlavor(Bus.class, "Bus");

    private MultiplexDescription description;
    private Bus bus;

    public MultiplexDescription getDescription() {
        return description;
    }

    public Bus getBus() {
        return bus;
    }

    public MultiplexDescriptionNode(MultiplexDescription signalDescription, Bus bus) {
	super(Children.create(new SignalNodeFactory(signalDescription, bus), true), Lookups.fixed(signalDescription, bus));

	this.description = signalDescription;
        this.bus = bus;

	setDisplayName(description.getName());
        setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/apps/accessories-calculator.png");
    }

    @Override
    public Transferable drag() {
        return this;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{BUS_DATA_FLAVOR, MULTIPLEX_DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == BUS_DATA_FLAVOR || flavor == MULTIPLEX_DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        if (flavor == BUS_DATA_FLAVOR) {
            return bus;
        } else if(flavor == MULTIPLEX_DATA_FLAVOR) {
            return description;
        }
        return null;
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = s.createPropertiesSet();

        Property byteOrder = new PropertySupport.ReadOnly<String>("Byte order", String.class, "Byte order", "Byte order of the signal") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                if(description.getByteOrder().equals(ByteOrder.BIG_ENDIAN)) {
		    return "Big endian";
		} else {
		    return "Little endian";
		}
            }

        };

        Property offset = new PropertySupport.ReadOnly<Integer>("Offset", Integer.class, "Offset", "Offset (in bit) of the signal in the frame") {

            @Override
            public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                return description.getOffset();
            }

        };

        Property length = new PropertySupport.ReadOnly<Integer>("Length", Integer.class, "Length", "Length (in bit) of the signal in the frame") {

            @Override
            public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                return description.getLength();
            }

        };

        set.put(byteOrder);
        set.put(offset);
        set.put(length);

        s.put(set);

        return s;
    }

}
