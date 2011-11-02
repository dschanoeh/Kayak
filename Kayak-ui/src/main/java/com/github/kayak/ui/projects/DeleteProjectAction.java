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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(id = "com.github.kayak.ui.projects.DeleteProjectAction", category = "File")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_DeleteProject", iconBase = "org/tango-project/tango-icon-theme/16x16/actions/edit-delete.png")
@ActionReferences(value = {
    @ActionReference(path = "Actions/Projects", position = 30 ),
    @ActionReference(path = "Menu/File", name = "com-github-kayak-ui-projects-NewProject", position = 30)})
public class DeleteProjectAction extends AbstractAction {

    private Project project;

    public DeleteProjectAction(Project p) {
            this.project = p;
            this.putValue(NAME, "Delete project");
        }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (project.isOpened()) {
            project.close();
        }

        ProjectManager.getGlobalProjectManager().removeProject(project);
    }

}
