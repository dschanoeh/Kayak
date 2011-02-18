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

package com.github.kayak.ui.projects;

import com.github.kayak.core.Bus;
import com.github.kayak.core.BusURL;
import java.util.ArrayList;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ProjectManager {
    private static ProjectManager projectManagement;
    private ArrayList<Project> projects;
    private ArrayList<ProjectChangeListener> listeners;

    public ArrayList<Project> getProjects() {
        return projects;
    }

    public void addProject(Project e) {
        projects.add(e);
        notifyListeners();
    }

    public void addListener(ProjectChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProjectChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for(ProjectChangeListener listener : listeners) {
            listener.projectChanged();
        }
    }

    public ProjectManager() {
        projects = new ArrayList<Project>();
        listeners = new ArrayList<ProjectChangeListener>();

        Project testProject = new Project("TestProject");
        Bus testBus = new Bus();
        testBus.setConnection(BusURL.fromString("socket://can0@127.0.0.1:28600"));
        testBus.setName("TestBus");
        testProject.addBus(testBus);
        projects.add(testProject);
    }

    public static ProjectManager getGlobalProjectManager() {
        if(projectManagement == null)
            projectManagement = new ProjectManager();

        return projectManagement;
    }
}
