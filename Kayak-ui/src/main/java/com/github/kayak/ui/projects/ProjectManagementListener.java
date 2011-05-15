/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.projects;

/**
 *
 * @author dschanoeh
 */
public interface ProjectManagementListener {

    public void projectsUpdated();
    
    public void openProjectChanged(Project p);
}
