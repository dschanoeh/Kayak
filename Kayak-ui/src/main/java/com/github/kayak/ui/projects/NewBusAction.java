/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.projects;

import com.github.kayak.core.Bus;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

@ActionRegistration(displayName="New bus...", iconBase="org/freedesktop/tango/16x16/places/network-workgroup.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="File", id="com.github.kayak.ui.projects.NewBusAction")
public final class NewBusAction extends AbstractAction {

    private final Project context;

    public NewBusAction(Project p) {
        this.putValue(NAME, "New bus...");
        this.context = p;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String name = JOptionPane.showInputDialog("Please give a name for the Bus", "newBus");

        if (name != null) {
            Bus b = new Bus();
            b.setName(name);
            context.addBus(b);
        }
    }
}
