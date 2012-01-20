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

@ActionRegistration(displayName="New bus...", iconBase="org/tango-project/tango-icon-theme/16x16/places/network-workgroup.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="File", id="com.github.kayak.ui.projects.NewBusAction")
@ActionReferences( value= {
    @ActionReference(path = "Shortcuts", name = "D-B"),
    @ActionReference(path = "Menu/File", position = 20 ),
    @ActionReference(path = "Actions/Projects", position = 20 )
})
public final class NewBusAction extends AbstractAction {

    private final Project context;

    public NewBusAction(Project p) {
        this.putValue(NAME, "New bus...");
        this.context = p;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Bus b = new Bus();
        b.setName(context.getNextValidBusName());

        String alias = JOptionPane.showInputDialog("Please give an alias for the Bus or leave blank for no alias", "");

        if (alias != null) {
            b.setAlias(alias);
        }

        context.addBus(b);
    }
}
