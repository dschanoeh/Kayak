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

package com.github.kayak.ui.statistics;

import com.github.kayak.core.Bus;
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
@ActionRegistration(displayName="Open bus statistics", iconBase="org/tango-project/tango-icon-theme/16x16/apps/utilities-system-monitor.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="BusViews", id="com.github.kayak.ui.statistics.OpenBusStatisticsAction")
@ActionReferences( value= {
    @ActionReference(path = "Menu/Bus views", position = 20 ),
    @ActionReference(path = "Toolbars/Bus views", position = 20 )
})
public final class OpenBusStatisticsAction extends AbstractAction {

    private final Bus context;

    public OpenBusStatisticsAction(Bus context) {
        this.putValue(NAME, "Open bus statistics");
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        BusStatisticsTopComponent tc = new BusStatisticsTopComponent();
        tc.setBus(context);
        tc.open();
        tc.requestActive();
    }
}
