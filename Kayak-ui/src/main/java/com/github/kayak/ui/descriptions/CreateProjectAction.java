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
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
@ActionRegistration(displayName="Create project...", iconBase="org/tango-project/tango-icon-theme/16x16/mimetypes/package-x-generic.png", iconInMenu=true, surviveFocusChange=true)
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

        for(BusDescription bd : context.getBusDescriptions()) {
            Bus b = new Bus();
            b.setName(p.getNextValidBusName());
            b.setAlias(bd.getName());
            b.setDescription(bd);
            p.addBus(b);
        }

        ProjectManager.getGlobalProjectManager().addProject(p);
    }
}
