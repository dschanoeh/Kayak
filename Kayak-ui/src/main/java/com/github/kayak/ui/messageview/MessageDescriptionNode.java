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
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.Node;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class MessageDescriptionNode extends AbstractNode implements Transferable {

    public static final DataFlavor MESSAGE_DATA_FLAVOR = new DataFlavor(MessageDescription.class, "MessageDescription");
    public static final DataFlavor BUS_DATA_FLAVOR = new DataFlavor(Bus.class, "Bus");

    private MessageDescription description;
    private Bus bus;

    public Bus getBus() {
        return bus;
    }

    public MessageDescription getDescription() {
        return description;
    }
    
    public MessageDescriptionNode(MessageDescription messageDescription, Bus bus) {
	super(Children.create(new SignalNodeFactory(messageDescription, bus), true), Lookups.fixed(messageDescription, bus));

	this.description = messageDescription;
        this.bus = bus;
        setDisplayName(description.getName());
        setIconBaseWithExtension("org/freedesktop/tango/16x16/apps/internet-mail.png");
    }

    @Override
    public Transferable drag() {
        return this;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{BUS_DATA_FLAVOR, MESSAGE_DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == BUS_DATA_FLAVOR || flavor == MESSAGE_DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        if (flavor == BUS_DATA_FLAVOR) {
            return bus;
        } else if(flavor == MESSAGE_DATA_FLAVOR) {
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
        
        Property producer = new PropertySupport.ReadOnly<String>("Producer", String.class, "Producer", "Node that produces this message") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                Node producer = description.getProducer();
                
		return producer.getName();	
            }

        };

        set.put(id);
        set.put(interval);
        set.put(producer);

        s.put(set);

        return s;
    } 
}
