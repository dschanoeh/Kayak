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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.AbstractNode;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
class BusNode extends AbstractNode {

    private Bus bus;
    private Project project;

    public BusNode(Bus bus, Project project) {
        super(new BusChildFactory(bus));

        setIconBaseWithExtension("org/freedesktop/tango/16x16/places/network-workgroup.png");
        setDisplayName(bus.getName());
        this.bus = bus;
        this.project = project;
    }

    @Override
    public Action[] getActions(boolean context) {

        return new Action[] { new RenameBusAction(), new DeleteBusAction() };
    }

    private class RenameBusAction extends AbstractAction {

        public RenameBusAction() {
            putValue(NAME, "Rename");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog("Please give a new name for the bus", bus.getName());

            if (name != null) {
                bus.setName(name);
            }
        }

    };

    private class DeleteBusAction extends AbstractAction {

        public DeleteBusAction() {
            putValue(NAME, "Delete");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove the bus?", "Are you sure?", JOptionPane.YES_NO_OPTION);

            if(result == JOptionPane.YES_OPTION) {
                project.removeBus(bus);
            }

        }

    };
};
