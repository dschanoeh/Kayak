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
package com.github.kayak.ui.events;

import com.github.kayak.core.Bus;
import com.github.kayak.core.EventFrame;
import com.github.kayak.core.EventFrameListener;
import com.github.kayak.ui.projects.Project;
import com.github.kayak.ui.projects.ProjectChangeListener;
import com.github.kayak.ui.projects.ProjectManagementListener;
import com.github.kayak.ui.projects.ProjectManager;
import java.awt.Component;
import javax.swing.JLabel;
import org.openide.awt.StatusLineElementProvider;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
@org.openide.util.lookup.ServiceProvider(service=StatusLineElementProvider.class)
public class EventsStatusElementProvider implements StatusLineElementProvider {
    private JLabel label;

    private ProjectManagementListener projectListener = new ProjectManagementListener() {

        @Override
        public void projectsUpdated() {
        }

        @Override
        public void openProjectChanged(Project p) {
            for(Bus b : p.getBusses()) {
                b.addEventFrameListener(eventFrameReceiver);
            }

            p.addProjectChangeListener(projectChangeListener);
        }
    };

    private ProjectChangeListener projectChangeListener = new ProjectChangeListener() {

        @Override
        public void projectNameChanged(Project p, String name) {

        }

        @Override
        public void projectClosed(Project p) {
            p.removeProjectChangeListener(projectChangeListener);
        }

        @Override
        public void projectOpened(Project p) {

        }

        @Override
        public void projectBusAdded(Project p, Bus bus) {
            bus.addEventFrameListener(eventFrameReceiver);
        }

        @Override
        public void projectBusRemoved(Project p, Bus bus) {
            bus.removeEventFrameListener(eventFrameReceiver);
        }
    };

    private EventFrameListener eventFrameReceiver = new EventFrameListener() {

        @Override
        public void newEventFrame(EventFrame f) {
            StringBuilder sb = new StringBuilder();
            long timestamp = f.getTimestamp();
            sb.append("(");
            sb.append(timestamp/1000);
            sb.append(".");
            sb.append(timestamp % 1000);
            sb.append(") ");
            sb.append(f.toString());

            label.setText(sb.toString());
        }
    };

    public EventsStatusElementProvider() {
        label = new JLabel();

        ProjectManager.getGlobalProjectManager().addListener(projectListener);

        Project p = ProjectManager.getGlobalProjectManager().getOpenedProject();

        if(p != null) {
            p.addProjectChangeListener(projectChangeListener);

            for(Bus b : p.getBusses()) {
                b.addEventFrameListener(eventFrameReceiver);
            }
        }
    }

    @Override
    public Component getStatusLineElement() {
        return label;
    }

}
