/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.projects;

/**
 *
 * @author dsi9mjn
 */
public interface ProjectChangeListener {

    public void projectNameChanged();

    public void projectClosed();

    public void projectOpened();

    public void projectDeleted();

    public void projectBussesChanged();
}
