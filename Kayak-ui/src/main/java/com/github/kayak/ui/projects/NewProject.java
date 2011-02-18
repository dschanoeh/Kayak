/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.projects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public final class NewProject implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = JOptionPane.showInputDialog("Please give a name for the Project", "myProject");

        if(name != null) {
            Project p = new Project(name);
            ProjectManager.getGlobalProjectManager().addProject(p);
        }
    }
}
