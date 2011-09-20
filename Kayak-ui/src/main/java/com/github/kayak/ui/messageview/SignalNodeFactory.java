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
import java.util.List;
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
        if(description != null) {
            list.addAll(description.getSignals());
            list.addAll(description.getMultiplexes());
        } else if(multiplexDescription != null) {
            list.addAll(multiplexDescription.getAllSignalDescriptions());
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
