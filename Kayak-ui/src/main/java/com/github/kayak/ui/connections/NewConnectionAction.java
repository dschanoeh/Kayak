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

package com.github.kayak.ui.connections;

import com.github.kayak.core.BusURL;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(id = "com.github.kayak.ui.connections.NewConnectionAction", category = "Connections")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_NewConnectionAction", iconBase = "org/tango-project/tango-icon-theme/16x16/actions/document-new.png")
@ActionReferences(value = {
    @ActionReference(path = "Menu/Connections", position = 20)})
public final class NewConnectionAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ev) {
        String url = JOptionPane.showInputDialog("Please specify a socket to connect with:", "socket://can0@192.168.30.129:29536");

        if (url != null) {
            BusURL beacon = BusURL.fromString(url);
            if (beacon != null) {
                ConnectionManager.getGlobalConnectionManager().addRecent(beacon);
            }
        }
    }
}
