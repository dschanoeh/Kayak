/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.MessageDescription;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dschanoeh
 */
public class MessageDescriptionNode extends AbstractNode implements Transferable {

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(MessageDescriptionNode.class, "MessageDescriptionNode");
    
    private MessageDescription description;

    public MessageDescription getDescription() {
        return description;
    }
    
    public MessageDescriptionNode(MessageDescription messageDescription) {
	super(Children.create(new SignalNodeFactory(messageDescription), true), Lookups.fixed(messageDescription));

	this.description = messageDescription;
        setDisplayName(description.getName());
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

        Property id = new PropertySupport.ReadOnly<String>("ID", String.class, "ID", "ID of the message") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return "0x" + Integer.toHexString(description.getId());
            }

        };

        Property interval = new PropertySupport.ReadOnly<Integer>("Interval", Integer.class, "Interval", "Interval of the message") {

            @Override
            public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                return description.getInterval();
            }

        };

        set.put(id);
        set.put(interval);

        s.put(set);

        return s;
    } 
}
