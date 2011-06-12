/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

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
    
    public static final DataFlavor DATA_FLAVOR = new DataFlavor(SignalDescriptionNode.class, "SignalDescriptionNode"); 
    
    private SignalDescription description;

    public SignalDescription getDescription() {
        return description;
    }
    
    public SignalDescriptionNode(SignalDescription signalDescription) {
	super(Children.LEAF, Lookups.fixed(signalDescription));

	this.description = signalDescription;

	setDisplayName(signalDescription.getName());
    }

        @Override
    public Transferable drag() {
        return this;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        if (flavor == DATA_FLAVOR) {
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
