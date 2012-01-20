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
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.core.description.DescriptionLoader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
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

    private static final Logger logger = Logger.getLogger(ProjectManager.class.getCanonicalName());

    private static ProjectManager projectManagement;
    private HashSet<Project> projects = new HashSet<Project>();
    private Project openedProject;
    private HashSet<ProjectManagementListener> listeners = new HashSet<ProjectManagementListener>();

    public Project getOpenedProject() {
        return openedProject;
    }

    public Set<Project> getProjects() {
        return Collections.unmodifiableSet(projects);
    }

    public void addProject(Project e) {
        projects.add(e);
        notifyListeners();
    }

    public void removeProject(Project e) {
        if(openedProject == e)
            closeProject(e);

        projects.remove(e);
        notifyListeners();
    }

    public void openProject(Project p) {
        if(!projects.contains(p) || p == openedProject)
            return;

        if(openedProject != null)
            closeProject(openedProject);

        openedProject = p;
        p.open();

        for(ProjectManagementListener l : listeners) {
            l.openProjectChanged(p);
        }
    }

    public void closeProject(Project p) {
        if(!projects.contains(p) || p != openedProject)
            return;

        openedProject.close();
        openedProject = null;
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

    /**
     * Returns the bus with the name busName if the project with the name
     * projectName is currently opened.
     * This may be used by persistent components that want to reconnect to
     * a bus.
     * @param projectName
     * @param busName
     * @return
     */
    public Bus findBus(String projectName, String busName) {

        if(openedProject != null && openedProject.getName().equals(projectName)) {
            Bus newBus = null;

            for (Bus b : openedProject.getBusses()) {
                if (b != null && b.getName() != null && b.getName().equals(busName)) {
                    newBus = b;
                    break;
                }
            }
            return newBus;
        }
        return null;
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

                    NodeList busses = projects.item(i).getChildNodes();

                    for(int j=0;j<busses.getLength();j++) {
                        Node busNode = busses.item(j);

                        NamedNodeMap busAttributes = busNode.getAttributes();
                        Node busNameNode = busAttributes.getNamedItem("name");
                        String busName = busNameNode.getNodeValue();
                        Node aliasNode = busAttributes.getNamedItem("alias");
                        String alias = aliasNode.getNodeValue();
                        Bus bus = new Bus();
                        bus.setName(busName);
                        bus.setAlias(alias);

                        NodeList busChildren = busNode.getChildNodes();

                        for(int k=0;k<busChildren.getLength();k++) {
                            if(busChildren.item(k).getNodeName().equals("Connection")) {
                                NamedNodeMap connectionAttributes =  busChildren.item(k).getAttributes();
                                BusURL connection = BusURL.fromString(connectionAttributes.getNamedItem("url").getNodeValue());
                                bus.setConnection(connection);
                            }

                            if(busChildren.item(k).getNodeName().equals("Description")) {
                                NamedNodeMap descriptionAttributes = busChildren.item(k).getAttributes();
                                String fileName = descriptionAttributes.getNamedItem("fileName").getNodeValue();
                                String descriptionName = descriptionAttributes.getNamedItem("name").getNodeValue();

                                File file = new File(fileName);
                                FileObject fileObject = FileUtil.toFileObject(file);

                                if(file.canRead()) {

                                    Collection<? extends DescriptionLoader> loaders = Lookup.getDefault().lookupAll(DescriptionLoader.class);

                                    for(DescriptionLoader loader : loaders) {
                                        String[] extensions = loader.getSupportedExtensions();
                                        for(String ext : extensions) {
                                            if(ext.equals(fileObject.getExt())) {
                                                com.github.kayak.core.description.Document parseFile = loader.parseFile(file);
                                                Set<BusDescription> busDescriptions = parseFile.getBusDescriptions();

                                                for(BusDescription b : busDescriptions)  {
                                                    if(b.getName().equals(descriptionName)) {
                                                        bus.setDescription(b);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        project.addBus(bus);
                    }

                    this.projects.add(project);

                    Node openedNode = attributes.getNamedItem("opened");
                    boolean opened = Boolean.parseBoolean(openedNode.getNodeValue());
                    if(opened)
                        openProject(project);
                }
            }

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not load projects", ex);
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
                    busElement.setAttribute("alias", bus.getAlias());
                    projectElement.appendChild(busElement);

                    BusURL connection = bus.getConnection();

                    if(connection != null) {
                        Element connectionElement = doc.createElement("Connection");
                        connectionElement.setAttribute("url", connection.toURLString());
                        busElement.appendChild(connectionElement);
                    }

                    BusDescription desc = bus.getDescription();

                    if(desc != null) {
                        Element descriptionElement = doc.createElement("Description");
                        descriptionElement.setAttribute("fileName", desc.getDocument().getFileName());
                        descriptionElement.setAttribute("name", desc.getName());
                        busElement.appendChild(descriptionElement);
                    }

                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not save projects", ex);
        }
    }
}
