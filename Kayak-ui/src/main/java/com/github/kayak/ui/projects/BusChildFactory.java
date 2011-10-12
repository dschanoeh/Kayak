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
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class BusChildFactory extends Children.Keys<BusChildFactory.Folders> {

    public enum Folders {
        CONNECTION, DESCRIPTION;
    }
    private Bus bus;
    private Project project;

    public BusChildFactory(Bus bus, Project project) {
        this.bus = bus;
        this.project = project;
    }

    @Override
    public void addNotify() {
        setKeys(new Folders[] {Folders.CONNECTION, Folders.DESCRIPTION});
    }

    @Override
    protected Node[] createNodes(Folders key) {
        if (key == Folders.CONNECTION) {
            return new Node[]{new ConnectedBusURLNode(bus.getConnection(), bus, project)};
        } else if (key == Folders.DESCRIPTION) {
            return new Node[]{ new ConnectedDescriptionNode(bus) };
        }

        return null;
    }

}
