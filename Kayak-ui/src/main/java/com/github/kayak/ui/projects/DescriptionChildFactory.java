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
package com.github.kayak.ui.projects;

import com.github.kayak.core.Bus;
import com.github.kayak.core.BusChangeListener;
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.ui.descriptions.DescriptionNode;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class DescriptionChildFactory extends ChildFactory<BusDescription> {

    private Bus bus;

    private BusChangeListener listener = new BusChangeListener() {

        @Override
        public void connectionChanged() {

        }

        @Override
        public void nameChanged(String name) {

        }

        @Override
        public void destroyed() {

        }

        @Override
        public void descriptionChanged() {
            refresh(true);
        }

        @Override
        public void aliasChanged(String string) {
            
        }
    };

    public DescriptionChildFactory(Bus bus) {
        this.bus = bus;
        bus.addBusChangeListener(listener);
    }

    @Override
    protected boolean createKeys(List<BusDescription> list) {
        BusDescription desc = bus.getDescription();
        if(desc != null)
            list.add(desc);
        return true;
    }

    @Override
    protected Node[] createNodesForKey(BusDescription key) {
        return new Node[] { new DescriptionNode(key) };
    }

}
