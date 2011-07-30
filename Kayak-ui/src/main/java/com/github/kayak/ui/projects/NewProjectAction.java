/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.projects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(id = "com.github.kayak.ui.projects.NewProjectAction", category = "File")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_NewProject", iconBase = "org/freedesktop/tango/16x16/mimetypes/package-x-generic.png")
@ActionReferences(value = {
    @ActionReference(path = "Shortcuts", name = "D-N"),
    @ActionReference(path = "Actions/Projects", position = 100 ),
    @ActionReference(path = "Menu/File", name = "com-github-kayak-ui-projects-NewProject", position = 100)})
public final class NewProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = JOptionPane.showInputDialog("Please give a name for the Project", "myProject");

        if(name != null) {
            ProjectManager manager = ProjectManager.getGlobalProjectManager();
            Project p = new Project(name);
            manager.addProject(p);
            if(manager.getOpenedProject() == null)
                manager.openProject(p);
        }
    }
}
