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

package com.github.kayak.logging;

import com.github.kayak.core.LogFile;
import com.github.kayak.logging.options.Options;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFileManager {
    
    private static final Logger logger = Logger.getLogger(LogFileManager.class.getCanonicalName());
    private static LogFileManager manager;

    private HashMap<String,HashSet<LogFile>> platformList;
    private TreeSet<String> platforms;
    private ArrayList<LogFileManagementChangeListener> listeners = new ArrayList<LogFileManagementChangeListener>();
    private ArrayList<LogFile> favourites = new ArrayList<LogFile>();
    private FileObject logFolder;
    
    private FileChangeListener changeListener = new FileChangeListener() {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            FileObject file = fe.getFile();
            if(file.getNameExt().endsWith(".log") || file.getNameExt().endsWith(".log.gz")) {
                LogFile l;
                try {
                    l = new LogFile(FileUtil.toFile(file));
                    addLogFile(l);
                    logger.log(Level.INFO, "New log file added");
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            FileObject file = fe.getFile();
            if(file.getNameExt().endsWith(".log") || file.getNameExt().endsWith(".log.gz")) {
                try {
                    LogFile l = new LogFile(FileUtil.toFile(file));
                    removeLogFile(file.getPath());
                    addLogFile(l);
                    logger.log(Level.INFO, "Log file updated");
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            removeLogFile(fe.getFile().getPath());
            logger.log(Level.INFO, "Log file removed that was deleted");
        }

        @Override
        public void fileRenamed(FileRenameEvent fre) {
            
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fae) {
            
        }
    };

    public TreeSet<String> getPlatforms() {
        return platforms;
    }

    public String getLogFolder() {
        return logFolder.getPath();
    }
    
    public void changeLogFolder(FileObject folder) {
        logFolder.removeFileChangeListener(changeListener);
        platformList = new HashMap<String, HashSet<LogFile>>();
        platforms = new TreeSet<String>();
        favourites = new ArrayList<LogFile>();
        
        logFolder = folder;
        readDirectory();
    }
    
    public void addListener(LogFileManagementChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(LogFileManagementChangeListener listener) {
        listeners.remove(listener);
    }

    public LogFileManager() {
        logFolder = FileUtil.toFileObject(new File(Options.getLogFilesFolder()));

        platformList = new HashMap<String,HashSet<LogFile>>();
        platforms = new TreeSet<String>();

        readDirectory();
    }

    public static LogFileManager getGlobalLogFileManager() {
        if(manager == null)
            manager = new LogFileManager();

        return manager;
    }

    private void readDirectory() {
        logger.log(Level.INFO, "Opening folder {0}", logFolder.getPath());
        platforms.clear();

        if (logFolder.isFolder()) {
            Enumeration<? extends FileObject> children = logFolder.getChildren(true);

            while (children.hasMoreElements()) {
                FileObject file = children.nextElement();

                if (file.getNameExt().endsWith(".log") || file.getNameExt().endsWith(".log.gz")) {
                    try {
                        LogFile logFile = new LogFile(FileUtil.toFile(file));

                        if (platformList.containsKey(logFile.getPlatform())) {
                            HashSet<LogFile> platform = platformList.get(logFile.getPlatform());
                            platform.add(logFile);
                        } else {
                            HashSet<LogFile> platform = new HashSet<LogFile>();
                            platform.add(logFile);
                            platformList.put(logFile.getPlatform(), platform);
                            platforms.add(logFile.getPlatform());
                        }
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "Found malformed log file: {0}. Ignoring...", file.getName());
                    }
                }
            }
            
            logFolder.addRecursiveListener(changeListener);
        }
    }
    
    public void removeLogFile(String fileName) {
        for(String platform : platforms) {
            for(LogFile f : platformList.get(platform)) {
                String name = f.getFileName();
                if(name.equals(fileName)) {
                    removeLogFile(f);
                    return;
                }
            }
        }
    }
    
    public void removeLogFile(LogFile file) {
        if(platforms.contains(file.getPlatform())) {
            platformList.get(file.getPlatform()).remove(file);
            
            for(LogFileManagementChangeListener listener : listeners) {
                listener.logFilesForPlatformChanged(file.getPlatform());
            }
            
            if (favourites.contains(file)) {
                favourites.remove(file);

                for (LogFileManagementChangeListener listener : listeners) {
                    listener.favouritesChanged();
                }
            }
        }
    }
    
    public void addLogFile(LogFile file) {
        if (platforms.contains(file.getPlatform())) {
            platformList.get(file.getPlatform()).add(file);
        } else {
            HashSet<LogFile> platform = new HashSet<LogFile>();
            platform.add(file);
            platformList.put(file.getPlatform(), platform);
            platforms.add(file.getPlatform());
            for(LogFileManagementChangeListener listener : listeners) {
                listener.platformsChanged();
            }
        }
        
        for(LogFileManagementChangeListener listener : listeners) {
            listener.logFilesForPlatformChanged(file.getPlatform());
        }
    }

    public HashSet<LogFile> getFilesForPlatform(String platform) {
        if(platforms.contains(platform)) {
            return platformList.get(platform);
        } else
            return null;
    }
    
    public void addFavourite(LogFile file) {
        favourites.add(file);
        
        for(LogFileManagementChangeListener listener : listeners) {
            listener.favouritesChanged();
        }
    }
    
    public void removeFavourite(LogFile file) {
        favourites.remove(file);
        
        for(LogFileManagementChangeListener listener : listeners) {
            listener.favouritesChanged();
        }
    }
    
    public List<LogFile> getFavouries() {
        return Collections.unmodifiableList(favourites);
    }

}
