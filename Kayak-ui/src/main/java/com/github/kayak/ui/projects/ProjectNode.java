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
import com.github.kayak.ui.connections.ConnectionManager;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ProjectNode extends AbstractNode {

    private Project project;
    private ProjectChangeListener changeListener = new ProjectChangeListener() {

        @Override
        public void projectNameChanged(Project p, String name) {
        }

        @Override
        public void projectClosed(Project p) {
            setChildren(Children.LEAF);
            setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/places/folder.png");
        }

        @Override
        public void projectOpened(Project p) {
            setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/status/folder-open.png");
            setChildren(Children.create(new ProjectChildFactory(project), true));
        }

        @Override
        public void projectBusAdded(Project p, Bus bus) {

        }

        @Override
        public void projectBusRemoved(Project p, Bus bus) {

        }
    };

    public ProjectNode(Project project) {
        super(Children.LEAF, Lookups.fixed(project));
        setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/places/folder.png");

        this.project = project;
        project.addProjectChangeListener(changeListener);

        if (project.isOpened()) {
            setChildren(Children.create(new ProjectChildFactory(project), true));
            setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/status/folder-open.png");
        }
        super.setDisplayName(project.getName());

    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        try {
            final BusURL url = (BusURL) t.getTransferData(BusURL.DATA_FLAVOR);
            return new PasteType() {

                @Override
                public Transferable paste() throws IOException {
                    if (url.checkConnection()) {
                        Bus b = new Bus();
                        b.setConnection(url);
                        String newName = url.getBus();
                        if(project.isBusNameValid(newName)) {
                            b.setName(url.getBus());
                        } else {
                            b.setName(project.getNextValidBusName());
                        }

                        String alias = JOptionPane.showInputDialog("Please input a alias for the Bus", url.getBus());

                        if (alias != null) {

                            b.setAlias(alias);

                        }

                        project.addBus(b);
                        ConnectionManager.getGlobalConnectionManager().addRecent(url);
                    }
                    return null;
                }
            };
        } catch (UnsupportedFlavorException ex) {
        } catch (IOException ex) {
        }
        return null;
    }

    @Override
    public void setDisplayName(String s) {
        super.setDisplayName(s);
        project.setName(s);
    }

    @Override
    public Action[] getActions(boolean popup) {
        if (project.isOpened()) {
            return new Action[]{new NewBusAction(project), new RenameAction(), new DeleteProjectAction(project), new CloseAction()};
        } else {
            return new Action[]{new OpenAction(), new RenameAction(), new DeleteProjectAction(project)};
        }
    }


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

    private class OpenAction extends AbstractAction {

        public OpenAction() {
            putValue(NAME, "Open");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectManager.getGlobalProjectManager().openProject(project);
        }
    };

    private class CloseAction extends AbstractAction {

        public CloseAction() {
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectManager.getGlobalProjectManager().closeProject(project);
        }
    };
}
