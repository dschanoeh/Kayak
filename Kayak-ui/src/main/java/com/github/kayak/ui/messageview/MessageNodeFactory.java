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
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.core.description.MessageDescription;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class MessageNodeFactory extends ChildFactory<MessageDescription> {

    private BusDescription description;
    private Bus bus;

    public MessageNodeFactory(BusDescription description, Bus bus) {
	this.description = description;
        this.bus = bus;
    }

    @Override
    protected boolean createKeys(List<MessageDescription> list) {
        Map<Integer, MessageDescription> messages = description.getMessages();

        Set<MessageDescription> set = new TreeSet<MessageDescription>(MessageDescription.nameComparator);
	for(Integer key : description.getMessages().keySet()) {
	    set.add(description.getMessages().get(key));
	}

        list.addAll(set);

	return true;
    }

    @Override
    protected Node[] createNodesForKey(MessageDescription key) {
	return new Node[] { new MessageDescriptionNode(key, bus) };
    }

}
