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
@ActionRegistration(iconInMenu = true, displayName = "#CTL_NewProject", iconBase = "org/tango-project/tango-icon-theme/16x16/actions/document-new.png")
@ActionReferences(value = {
    @ActionReference(path = "Shortcuts", name = "D-N"),
    @ActionReference(path = "Actions/Projects", position = 10 ),
    @ActionReference(path = "Menu/File", name = "com-github-kayak-ui-projects-NewProject", position = 10)})
public final class NewProjectAction implements ActionListener {

    ProjectManager manager = ProjectManager.getGlobalProjectManager();

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = JOptionPane.showInputDialog("Please give a name for the Project", "myProject");

        if(name != null) {
            while(!nameValid(name)) {
                name = JOptionPane.showInputDialog("Project name already used. Please give a valid name for the Project", "myProject");
                if(name == null)
                    return;
            }

            Project p = new Project(name);
            manager.addProject(p);
            if(manager.getOpenedProject() == null)
                manager.openProject(p);
        }
    }

    private boolean nameValid(String name) {
        for(Project p : manager.getProjects()) {
            if(p.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }
}
