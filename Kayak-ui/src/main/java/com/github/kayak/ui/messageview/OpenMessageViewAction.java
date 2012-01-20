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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
@ActionID(category = "BusViews",
id = "com.github.kayak.ui.messageview.OpenMessageViewAction")
@ActionRegistration(displayName = "#CTL_OpenMessageViewAction",
        iconBase="org/tango-project/tango-icon-theme/16x16/status/dialog-information.png")
@ActionReferences(value = {
    @ActionReference(path = "Menu/Bus views", position = 5),
    @ActionReference(path = "Toolbars/Bus views", position = 5)
})
public final class OpenMessageViewAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        MessageViewTopComponent tc = new MessageViewTopComponent();
        tc.open();
        tc.requestActive();
    }
}
