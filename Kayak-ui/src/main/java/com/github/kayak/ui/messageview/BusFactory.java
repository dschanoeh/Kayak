/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.BusChangeListener;
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.ui.projects.Project;
import com.github.kayak.ui.projects.ProjectChangeListener;
import com.github.kayak.ui.projects.ProjectManagementListener;
import com.github.kayak.ui.projects.ProjectManager;
import java.util.List;
import java.util.Set;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dschanoeh
 */
public class BusFactory extends ChildFactory<BusDescription> {

    Project project;

    private ProjectManagementListener projectListener = new ProjectManagementListener() {

        @Override
        public void projectsUpdated() {
        }

        @Override
        public void openProjectChanged(Project p) {
            project = p;
            project.addProjectChangeListener(changeListener);
            for(Bus b : p.getBusses()) {
                b.addBusChangeListener(busListener);
            }
            refresh(true);
        }
    };

    private ProjectChangeListener changeListener = new ProjectChangeListener() {

        @Override
        public void projectNameChanged(Project p, String name) {
        }

        @Override
        public void projectClosed(Project p) {
            p.removeProjectChangeListener(changeListener);
            for(Bus b : p.getBusses()) {
                b.removeBusChangeListener(busListener);
            }
            project = null;
            refresh(true);
        }

        @Override
        public void projectOpened(Project p) {
        }

        @Override
        public void projectBusAdded(Project p, Bus bus) {
            bus.addBusChangeListener(busListener);
            refresh(true);
        }

        @Override
        public void projectBusRemoved(Project p, Bus bus) {
            bus.removeBusChangeListener(busListener);
            refresh(true);
        }
    };

    private BusChangeListener busListener = new BusChangeListener() {

        @Override
        public void connectionChanged() {
        }

        @Override
        public void nameChanged(String name) {
        }

        @Override
        public void destroyed() {
        }

        @Override
        public void descriptionChanged() {
            refresh(true);
        }

        @Override
        public void aliasChanged(String string) {

        }
    };

    public BusFactory() {
        ProjectManager.getGlobalProjectManager().addListener(projectListener);

        project = ProjectManager.getGlobalProjectManager().getOpenedProject();
        if(project != null) {
            project.addProjectChangeListener(changeListener);
            for(Bus b : project.getBusses()) {
                b.addBusChangeListener(busListener);
            }
        }
    }

    @Override
    protected boolean createKeys(List<BusDescription> list) {
        if(project != null) {
            Set<Bus> busses = project.getBusses();

            for(Bus b : busses) {
                BusDescription desc = b.getDescription();
                if(desc != null) {
                    list.add(desc);
                }
            }
        }

        return true;
    }

    @Override
    protected Node[] createNodesForKey(BusDescription key) {
        Bus bus = null;
        for(Bus b : project.getBusses()) {
            if(b.getDescription() == key)
                bus = b;
        }

        AbstractNode node = new AbstractNode(Children.create(new MessageNodeFactory(key, bus), true), Lookups.fixed(key, bus));
        node.setIconBaseWithExtension("org/freedesktop/tango/16x16/places/network-workgroup.png");
        node.setDisplayName(bus.getName() + " (" + key.getName() + ")");
        return new Node[] { node };
    }

}
