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

package com.github.kayak.ui;

import com.github.kayak.ui.connections.ConnectionManager;
import com.github.kayak.ui.descriptions.DescriptionManagement;
import com.github.kayak.ui.projects.ProjectManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ModuleLifecycleManager extends ModuleInstall {

    private static final Logger logger = Logger.getLogger(ModuleLifecycleManager.class.getCanonicalName());

    @Override
    public void restored() {
        readConnections();
        readProjects();

        /* make sure that the required folders exist */
        String homeFolder = System.getProperty("user.home");

        String logDir = NbPreferences.forModule(ModuleLifecycleManager.class).get("Log file directory", homeFolder + "/kayak/log/");
        File logDirFile = new File(logDir);
        if(!logDirFile.exists()) {
            logger.log(Level.INFO, "Log dir does not exist. creating...");
            if(!logDirFile.mkdirs()) {
                logger.log(Level.SEVERE, "Could not create directory!");
            }
        }

        String descriptionDir = NbPreferences.forModule(ModuleLifecycleManager.class).get("Bus description directory", homeFolder + "/kayak/descriptions/");
        File descriptionDirFile = new File(descriptionDir);
        if(!descriptionDirFile.exists()) {
            logger.log(Level.INFO, "Bus description dir does not exist. creating...");
            if(!descriptionDirFile.mkdirs()) {
                logger.log(Level.SEVERE, "Could not create directory!");
            }
        }
    }

    @Override
    public void close() {
        super.close();

        writeConnections();
        writeProjects();
    }

    private void writeConnections() {
        FileObject root = FileUtil.getConfigRoot();

        FileObject connectionStorage = root.getFileObject("Connections.xml");
        if (connectionStorage == null) {
            try {
                connectionStorage = root.createData("Connections.xml");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        FileLock lock;
        try {
            lock = connectionStorage.lock();

            try {
                OutputStream stream = connectionStorage.getOutputStream(lock);
                ConnectionManager.getGlobalConnectionManager().writeToFile(stream);
                stream.close();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                lock.releaseLock();
            }
        } catch(IOException ex) {
        }
    }

    private void writeProjects() {
        FileObject root = FileUtil.getConfigRoot();

        FileObject projectStorage = root.getFileObject("Projects.xml");
        if (projectStorage == null) {
            try {
                projectStorage = root.createData("Projects.xml");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        FileLock lock;
        try {
            lock = projectStorage.lock();

            try {
                OutputStream stream = projectStorage.getOutputStream(lock);
                ProjectManager.getGlobalProjectManager().writeToFile(stream);
                stream.close();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                lock.releaseLock();
            }
        } catch(IOException ex) {
        }
    }

    private void readConnections() {
        FileObject root = FileUtil.getConfigRoot();

        FileObject connectionStorage = root.getFileObject("Connections.xml");
        if(connectionStorage==null)
            return;

        FileLock lock;
        try {
            lock = connectionStorage.lock();

            try {
                InputStream stream = connectionStorage.getInputStream();
                ConnectionManager.getGlobalConnectionManager().loadFromFile(stream);
                stream.close();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                lock.releaseLock();
            }
        } catch(IOException ex) {
        }
    }

    private void readProjects() {
        FileObject root = FileUtil.getConfigRoot();

        FileObject projectStorage = root.getFileObject("Projects.xml");
        if(projectStorage==null)
            return;

        FileLock lock;
        try {
            lock = projectStorage.lock();

            try {
                InputStream stream = projectStorage.getInputStream();
                ProjectManager.getGlobalProjectManager().loadFromFile(stream);
                stream.close();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                lock.releaseLock();
            }
        } catch(IOException ex) {
        }
    }
}
