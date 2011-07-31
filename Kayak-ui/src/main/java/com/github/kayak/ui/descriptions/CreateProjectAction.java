/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.descriptions;

import com.github.kayak.core.Bus;
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.core.description.Document;
import com.github.kayak.ui.projects.Project;
import com.github.kayak.ui.projects.ProjectManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author dschanoeh
 */
@ActionRegistration(displayName="Create project...", iconBase="org/freedesktop/tango/16x16/mimetypes/package-x-generic.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="Descriptions", id="com.github.kayak.ui.descriptions.CreateProjectAction")
@ActionReferences(value = {
    @ActionReference(path="Menu/Descriptions", position=30)}) 
public class CreateProjectAction extends AbstractAction {
    
    private Document context;
    
    public CreateProjectAction(DocumentNode context) {
        putValue(NAME, "Create project...");
        this.context = context.getLookup().lookup(Document.class);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Project p = new Project(context.getName());

        for(BusDescription bd : context.getBusses()) {
            Bus b = new Bus();
            b.setName(bd.getName());
            b.setDescription(bd);
            p.addBus(b);
        }

        ProjectManager.getGlobalProjectManager().addProject(p);
    } 
}
