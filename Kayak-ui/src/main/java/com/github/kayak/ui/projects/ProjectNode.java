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
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ProjectNode extends AbstractNode implements NewBusCookie {

    private Project project;
    private ProjectChangeListener changeListener = new ProjectChangeListener() {

        @Override
        public void projectNameChanged() {
        }

        @Override
        public void projectClosed() {
            setChildren(Children.LEAF);
            setIconBaseWithExtension("org/freedesktop/tango/16x16/places/folder.png");
        }

        @Override
        public void projectOpened() {
            setIconBaseWithExtension("org/freedesktop/tango/16x16/status/folder-open.png");
            setChildren(Children.create(new ProjectChildFactory(project), true));
        }

        @Override
        public void projectBusAdded(Bus bus) {
            
        }

        @Override
        public void projectBusRemoved(Bus bus) {
            
        }
    };

    public ProjectNode(Project project) {
        super(Children.LEAF);
        setIconBaseWithExtension("org/freedesktop/tango/16x16/places/folder.png");

        this.project = project;
        project.addProjectChangeListener(changeListener);

        if (project.isOpened()) {
            setChildren(Children.create(new ProjectChildFactory(project), true));
            setIconBaseWithExtension("org/freedesktop/tango/16x16/status/folder-open.png");
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
                        String name = JOptionPane.showInputDialog("Please give a name for the Bus", url.getBus());

                        if (name != null) {
                            Bus b = new Bus();
                            b.setName(name);
                            project.addBus(b);
                            b.setConnection(url);
                            ConnectionManager.getGlobalConnectionManager().addRecent(url);
                        }
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
    public void addNewBus() {
        String name = JOptionPane.showInputDialog("Please give a name for the Bus", "newBus");

        if (name != null) {
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
        if (project.isOpened()) {
            return new Action[]{new RenameAction(), new DeleteAction(), new CloseAction()};
        } else {
            return new Action[]{new RenameAction(), new DeleteAction(), new OpenAction()};
        }
    }

    private class DeleteAction extends AbstractAction {

        public DeleteAction() {
            putValue(NAME, "Delete");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (project.isOpened()) {
                project.close();
            }

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
