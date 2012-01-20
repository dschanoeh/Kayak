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
import com.github.kayak.core.description.MultiplexDescription;
import com.github.kayak.core.description.SignalDescription;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class SignalNodeFactory extends ChildFactory<Object> {

    private MessageDescription description = null;
    private MultiplexDescription multiplexDescription = null;
    private Bus bus;

    private static final Comparator<Object> comparator = new Comparator<Object>() {

        @Override
        public int compare(Object o1, Object o2) {
            String name1="";
            String name2="";

            if(o1 instanceof SignalDescription) {
                name1 = ((SignalDescription) o1).getName();
            } else if(o1 instanceof MultiplexDescription) {
                name1 = ((MultiplexDescription) o1).getName();
            }

            if(o2 instanceof SignalDescription) {
                name2 = ((SignalDescription) o2).getName();
            } else if(o2 instanceof MultiplexDescription) {
                name2 = ((MultiplexDescription) o2).getName();
            }

            return name1.compareTo(name2);
        }

    };

    public SignalNodeFactory(MessageDescription description, Bus bus) {
        this.description = description;
        this.bus = bus;
    }

    public SignalNodeFactory(MultiplexDescription multiplex, Bus bus) {
        this.bus = bus;
        this.multiplexDescription = multiplex;
    }


    @Override
    protected boolean createKeys(List<Object> list) {
        Set<Object> set = new TreeSet<Object>(comparator);
        if(description != null) {
            set.addAll(description.getSignals());
            set.addAll(description.getSignals());
            set.addAll(description.getMultiplexes());
            list.addAll(set);
        } else if(multiplexDescription != null) {
            set.addAll(multiplexDescription.getAllSignalDescriptions());
            list.addAll(set);
        }

        return true;
    }

    @Override
    protected Node[] createNodesForKey(Object key) {

        if(key instanceof SignalDescription)
            return new Node[] { new SignalDescriptionNode((SignalDescription) key, bus) };
        else if(key instanceof MultiplexDescription) {
            return new Node[] { new MultiplexDescriptionNode((MultiplexDescription) key, bus) };
        } else
            return null;
    }

}
