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

package com.github.kayak.ui.gaugeview;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
@ActionRegistration(displayName="Open gauge view", iconBase="org/tango-project/tango-icon-theme/16x16/actions/appointment-new.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="BusViews", id="com.github.kayak.ui.gaugeview.OpenGaugeViewAction")
@ActionReferences( value= {
    @ActionReference(path = "Menu/Bus views", position = 10),
    @ActionReference(path = "Toolbars/Bus views", position = 10)
})
public class OpenGaugeViewAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent ev) {
        GaugeTopComponent tc = new GaugeTopComponent();
        tc.open();
        tc.requestActive();
    }
}
