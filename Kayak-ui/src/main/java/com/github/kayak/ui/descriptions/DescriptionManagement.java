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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class DescriptionManagement {
    
    private static final Logger logger = Logger.getLogger(DescriptionManagement.class.getCanonicalName());
    
    private static DescriptionManagement instance;
    private FileObject descriptionsFolder;
    private ArrayList<Document> descriptions = new ArrayList<Document>();
    
    private DescriptionManagement() {
        descriptionsFolder = FileUtil.toFileObject(new File(Options.getDescriptionsFolder()));
        
        
        
        Collection<? extends DescriptionLoader> loaders = Lookup.getDefault().lookupAll(DescriptionLoader.class);
        
        logger.log(Level.INFO, "Found {0} DescriptionLoaders.", Integer.toString(loaders.size()));
        
        if (descriptionsFolder.isFolder()) {
            Enumeration<? extends FileObject> children = descriptionsFolder.getChildren(true);

            while (children.hasMoreElements()) {
                FileObject file = children.nextElement();

                try {
                    for(DescriptionLoader loader : loaders) {
                        for(String extension : loader.getSupportedExtensions()) {
                            if(file.getExt().equals(extension)) {
                                Document parseFile = loader.parseFile(FileUtil.toFile(file));
                                descriptions.add(parseFile);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Could not parse description", ex);
                }
            }
        }
    }
    
    public static DescriptionManagement getGlobalDescriptionManagement() {
        if(instance == null)
            instance = new DescriptionManagement();
        
        return instance;
    }
    
    public Collection<Document> getDescriptions() {
        return descriptions;
    }
    
}
