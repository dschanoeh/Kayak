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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ProjectManager {

    private static ProjectManager projectManagement;
    private ArrayList<Project> projects;
    private Project openedProject;
    private ArrayList<ProjectManagementListener> listeners;

    public Project getOpenedProject() {
        return openedProject;
    }

    public ArrayList<Project> getProjects() {
        return projects;
    }

    public void addProject(Project e) {
        projects.add(e);
        notifyListeners();
    }

    public void removeProject(Project e) {
        projects.remove(e);
        notifyListeners();
    }

    public void openProject(Project p) {
        if(!projects.contains(p) || p == openedProject)
            return;

        if(openedProject != null)
            openedProject.close();

        p.open();
        openedProject = p;
    }

    public void closeProject(Project p) {
        if(!projects.contains(p) || p != openedProject)
            return;

        openedProject.close();
    }

    public void addListener(ProjectManagementListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProjectManagementListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for(ProjectManagementListener listener : listeners) {
            listener.projectsUpdated();
        }
    }

    public ProjectManager() {
        projects = new ArrayList<Project>();
        listeners = new ArrayList<ProjectManagementListener>();
    }

    public static ProjectManager getGlobalProjectManager() {
        if(projectManagement == null)
            projectManagement = new ProjectManager();

        return projectManagement;
    }

    public void loadFromFile(InputStream stream) {
    try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(stream);

            NodeList projectsList = doc.getElementsByTagName("Projects");
            if(projectsList.getLength()==1) {
                Node projectsNode = projectsList.item(0);
                NodeList projects = projectsNode.getChildNodes();

                for(int i = 0; i < projects.getLength(); i++) {

                    NamedNodeMap attributes = projects.item(i).getAttributes();
                    Node nameNode = attributes.getNamedItem("name");
                    String name = nameNode.getNodeValue();
                    Project project = new Project(name);
                    
                    Node openedNode = attributes.getNamedItem("opened");
                    boolean opened = Boolean.parseBoolean(openedNode.getNodeValue());
                    if(opened)
                        openProject(project);

                    NodeList busses = projects.item(i).getChildNodes();

                    for(int j=0;j<busses.getLength();j++) {
                        Node busNode = busses.item(j);

                        NamedNodeMap busAttributes = busNode.getAttributes();
                        Node busNameNode = busAttributes.getNamedItem("name");

                        String busName = busNameNode.getNodeValue();
                        Bus bus = new Bus();
                        bus.setName(busName);

                        NodeList busChildren = busNode.getChildNodes();

                        for(int k=0;k<busChildren.getLength();k++) {
                            if(busChildren.item(k).getNodeName().equals("Connection")) {
                                NamedNodeMap connectionAttributes =  busChildren.item(k).getAttributes();
                                BusURL connection = BusURL.fromString(connectionAttributes.getNamedItem("url").getNodeValue());
                                bus.setConnection(connection);
                            }
                        }

                        project.addBus(bus);
                    }


                    this.projects.add(project);

                }
            }

        } catch (Exception ex) {
            //logOutput.getErr().write("Error while reading connections from file\n");
        }
    }

    public void writeToFile(OutputStream stream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("Projects");
            doc.appendChild(root);

            for(Project project : projects) {
                Element projectElement = doc.createElement("Project");
                projectElement.setAttribute("name", project.getName());
                projectElement.setAttribute("opened", Boolean.toString(project.isOpened()));
                root.appendChild(projectElement);

                for(Bus bus : project.getBusses()) {
                    Element busElement = doc.createElement("Bus");
                    busElement.setAttribute("name", bus.getName());
                    projectElement.appendChild(busElement);

                    BusURL connection = bus.getConnection();

                    if(connection != null) {
                        Element connectionElement = doc.createElement("Connection");
                        connectionElement.setAttribute("url", connection.toURLString());
                        busElement.appendChild(connectionElement);
                    }

                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
        } catch (Exception ex) {
            //logOutput.getErr().write("Error while writing connections to file\n");
        }
    }
}
