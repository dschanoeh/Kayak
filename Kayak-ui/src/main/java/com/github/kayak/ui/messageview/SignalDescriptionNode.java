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
import com.github.kayak.core.description.SignalDescription;
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
 * @author dschanoeh
 */
public class SignalDescriptionNode extends AbstractNode implements Transferable {
    
    public static final DataFlavor SIGNAL_DATA_FLAVOR = new DataFlavor(SignalDescription.class, "SignalDescription"); 
    public static final DataFlavor BUS_DATA_FLAVOR = new DataFlavor(Bus.class, "Bus"); 
    
    private SignalDescription description;
    private Bus bus;

    public SignalDescription getDescription() {
        return description;
    }

    public Bus getBus() {
        return bus;
    }
    
    public SignalDescriptionNode(SignalDescription signalDescription, Bus bus) {
	super(Children.LEAF, Lookups.fixed(signalDescription, bus));

	this.description = signalDescription;
        this.bus = bus;

	setDisplayName(signalDescription.getName());
        setIconBaseWithExtension("org/freedesktop/tango/16x16/status/dialog-information.png");
    }

    @Override
    public Transferable drag() {
        return this;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{BUS_DATA_FLAVOR, SIGNAL_DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == BUS_DATA_FLAVOR || flavor == SIGNAL_DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        if (flavor == BUS_DATA_FLAVOR) {
            return bus;
        } else if(flavor == SIGNAL_DATA_FLAVOR) {
            return description;
        }
        return null;
    } 

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = s.createPropertiesSet();

        Property notes = new PropertySupport.ReadOnly<String>("Notes", String.class, "Notes", "Signal description notes") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return description.getNotes();
            }

        };

        Property unit = new PropertySupport.ReadOnly<String>("Unit", String.class, "Unit", "Unit of the signal") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return description.getUnit();
            }

        };

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

        Property type = new PropertySupport.ReadOnly<String>("Type", String.class, "Type", "Type of the signal") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                SignalDescription.Type type =  description.getType();

		switch(type) {
		    case DOUBLE:
			return "Double";
		    case SIGNED:
			return "Signed";
		    case SINGLE:
			return "Single";
		    case UNSIGNED:
			return "Unsigned";
		    default:
			return "";
		}
			
            }

        };

        set.put(notes);
        set.put(type);
        set.put(unit);
        set.put(byteOrder);

        s.put(set);

        return s;
    }
    
}
