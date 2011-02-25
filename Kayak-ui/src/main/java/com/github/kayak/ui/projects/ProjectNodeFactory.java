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
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openide.actions.RenameAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ProjectNodeFactory extends ChildFactory<Project> implements ProjectChangeListener {
    private ProjectManager manager = ProjectManager.getGlobalProjectManager();

    @Override
    protected boolean createKeys(List<Project> toPopulate) {
        for(Project p : manager.getProjects()) {
            toPopulate.add(p);
        }

        return true;
    }

    public ProjectNodeFactory() {
        manager.addListener(this);
    }

    @Override
    protected Node[] createNodesForKey(Project key) {
        ProjectNode node = new ProjectNode(key);
        return new Node[] { node };
    }

    @Override
    public void projectChanged() {
        refresh(true);
    }

    private class ProjectNode extends AbstractNode implements NewBusCookie {
        private Project project;

        public ProjectNode(Project project) {
            super(Children.create(new ProjectChildFactory(project), true));
            this.project = project;
            super.setDisplayName(project.getName());
            setIconBaseWithExtension("org/freedesktop/tango/16x16/mimetypes/package-x-generic.png");
        }

        @Override
        public void addNewBus() {
            String name = JOptionPane.showInputDialog("Please give a name for the Bus", "newBus");

            if(name != null) {
                Bus b = new Bus();
                b.setName(name);
                project.addBus(b);
            }
        }

        @Override
        public void setDisplayName(String s) {
            super.setDisplayName(s);
            project.setName(s);
        }

        @Override
        public Action[] getActions(boolean popup) {
            return new Action[] { new RenameAction(), new DeleteAction() };
        }

        private class DeleteAction extends AbstractAction {

            public DeleteAction() {
                putValue(NAME, "Delete");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectManager.getGlobalProjectManager().removeProject(project);
            }

        };

        private class RenameAction extends AbstractAction {

            public RenameAction() {
                putValue(NAME, "Rename...");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Please give a new name for the bus", project.getName());

                if (name != null) {
                    setDisplayName(name);
                }
            }

        };

    };

}
