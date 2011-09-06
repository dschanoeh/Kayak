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
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionRegistration(displayName="New bus...", iconBase="org/freedesktop/tango/16x16/places/network-workgroup.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="File", id="com.github.kayak.ui.projects.NewBusAction")
@ActionReferences( value= {
    @ActionReference(path = "Shortcuts", name = "D-B"),
    @ActionReference(path = "Menu/File", position = 200 ),
    @ActionReference(path = "Actions/Projects", position = 200 )
})
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
            while(!nameValid(name)) {
                name = JOptionPane.showInputDialog("Bus name is already used. Please give a correct name for the Bus", "newBus");
                if(name == null)
                    return;
            }
            Bus b = new Bus();
            b.setName(name);
            context.addBus(b);
        }
    }
    
    private boolean nameValid(String name) {
        for(Bus b : context.getBusses()) {
            if(b.getName().equals(name))
                return false;
        }
        
        return true;
    }
}
