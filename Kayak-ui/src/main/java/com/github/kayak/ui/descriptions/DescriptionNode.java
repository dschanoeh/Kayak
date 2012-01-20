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
package com.github.kayak.ui.descriptions;

import com.github.kayak.core.description.BusDescription;
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
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class DescriptionNode extends AbstractNode implements Transferable {

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(DescriptionNode.class, "DescriptionNode");

    private BusDescription description;

    public BusDescription getDescription() {
        return description;
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
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (flavor == DATA_FLAVOR) {
            return description;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public DescriptionNode(BusDescription d) {
        super(Children.LEAF, Lookups.fixed(d));

        this.description = d;
        setDisplayName(description.getName());
        this.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/places/network-workgroup.png");
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property name = new PropertySupport.ReadOnly<String>("Name", String.class, "Name", "Name of the description") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return description.getName();
            }
        };

        Property baudrate = new PropertySupport.ReadOnly<Integer>("Baudrate", Integer.class, "Baudrate", "Predefined baudrate on the bus") {

            @Override
            public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                return description.getBaudrate();
            }
        };

        set.put(name);
        set.put(baudrate);

        s.put(set);

        return s;
    }

}
