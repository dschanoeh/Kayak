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

import com.github.kayak.core.description.DescriptionLoader;
import com.github.kayak.core.description.Document;
import com.github.kayak.ui.options.Options;
import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class DescriptionManagement {

    private static final Logger logger = Logger.getLogger(DescriptionManagement.class.getCanonicalName());

    private static DescriptionManagement instance;
    private FileObject descriptionsFolder;
    private Set<Document> descriptions = new HashSet<Document>();
    Collection<? extends DescriptionLoader> loaders;
    Set<DescriptionManagementChangeListener> listeners = new HashSet<DescriptionManagementChangeListener>();

    private FileChangeListener changeListener = new FileChangeListener() {

        @Override
        public void fileFolderCreated(FileEvent fe) {

        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            logger.log(Level.INFO, "Adding new description {0}", fe.getFile().getPath());
            addDescription(fe.getFile());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            logger.log(Level.INFO, "Changing description {0}", fe.getFile().getPath());
            removeDescription(fe.getFile());
            addDescription(fe.getFile());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            logger.log(Level.INFO, "Removing description {0}", fe.getFile().getPath());
            removeDescription(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fre) {

        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fae) {

        }
    };

    private Runnable initRunnable = new Runnable() {

        @Override
        public void run() {
            if (descriptionsFolder.isFolder()) {
            Enumeration<? extends FileObject> children = descriptionsFolder.getChildren(true);

            while (children.hasMoreElements()) {
                FileObject file = children.nextElement();
                addDescription(file);
            }
        }

        descriptionsFolder.addRecursiveListener(changeListener);
        }
    };

    private void addDescription(FileObject file) {
        for (DescriptionLoader loader : loaders) {
            for (String extension : loader.getSupportedExtensions()) {
                if (file.getExt().equals(extension)) {
                    try {
                        Document parseFile = loader.parseFile(FileUtil.toFile(file));
                        if(parseFile != null) {
                            descriptions.add(parseFile);
                            for (DescriptionManagementChangeListener listener : listeners) {
                                listener.descriptionRemoved(parseFile);
                            }
                        }
                    } catch (Exception ex) {
                        logger.log(Level.INFO, "Could not load file", ex);
                    }
                }
            }
        }
    }

    private void removeDescription(FileObject fe) {
        Document doc = null;
        for (Document d : descriptions) {
            if (d.getFileName().equals(fe.getPath())) {
                doc = d;
                break;

            }
        }
        if (doc != null) {
            descriptions.remove(doc);
            for (DescriptionManagementChangeListener listener : listeners) {
                listener.descriptionRemoved(doc);
            }
        }
    }

    private DescriptionManagement() {
        descriptionsFolder = FileUtil.toFileObject(new File(Options.getDescriptionsFolder()));

        loaders = Lookup.getDefault().lookupAll(DescriptionLoader.class);
        logger.log(Level.INFO, "Found {0} DescriptionLoaders.", Integer.toString(loaders.size()));

        Task t = new Task(initRunnable);
        RequestProcessor.getDefault().post(t);
    }

    public static DescriptionManagement getGlobalDescriptionManagement() {
        if(instance == null)
            instance = new DescriptionManagement();

        return instance;
    }

    public Collection<Document> getDescriptions() {
        return descriptions;
    }

    public void addListener(DescriptionManagementChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DescriptionManagementChangeListener listener) {
        listeners.remove(listener);
    }
}
