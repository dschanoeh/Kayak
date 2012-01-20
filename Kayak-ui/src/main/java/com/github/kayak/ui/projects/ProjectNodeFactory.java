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

import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ProjectNodeFactory extends ChildFactory<Project> {

    private ProjectManager manager = ProjectManager.getGlobalProjectManager();
    
    private ProjectManagementListener listener = new ProjectManagementListener() {

        @Override
        public void projectsUpdated() {
            refresh(true);
        }

        @Override
        public void openProjectChanged(Project p) {
        }
    };

    @Override
    protected boolean createKeys(List<Project> toPopulate) {
        toPopulate.addAll(manager.getProjects());
        
        return true;
    }

    public ProjectNodeFactory() {
        manager.addListener(listener);
    }

    @Override
    protected Node[] createNodesForKey(Project key) {
        ProjectNode node = new ProjectNode(key);
        return new Node[] { node };
    }  

}
