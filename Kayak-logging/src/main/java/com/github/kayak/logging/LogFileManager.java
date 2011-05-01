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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFileManager {
    
    private static final Logger logger = Logger.getLogger(LogFileManager.class.getCanonicalName());
    private static LogFileManager manager;

    private HashMap<String,ArrayList<LogFile>> platformList;
    private TreeSet<String> platforms;
    private ArrayList<LogFileManagementChangeListener> listeners = new ArrayList<LogFileManagementChangeListener>();
    private ArrayList<LogFile> favourites = new ArrayList<LogFile>();

    public TreeSet<String> getPlatforms() {
        return platforms;
    }

    private ArrayList<LogFile> logFileDir;
    private FileObject logFolder;

    public String getLogFolder() {
        return logFolder.getPath();
    }
    
    public void addListener(LogFileManagementChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(LogFileManagementChangeListener listener) {
        listeners.remove(listener);
    }

    public LogFileManager() {
        logFolder = FileUtil.toFileObject(new File(Options.getLogFilesFolder()));

        logFileDir = new ArrayList<LogFile>();
        platformList = new HashMap<String,ArrayList<LogFile>>();
        platforms = new TreeSet<String>();

        readDirectory();
    }

    public static LogFileManager getGlobalLogFileManager() {
        if(manager == null)
            manager = new LogFileManager();

        return manager;
    }

    public void readDirectory() {
        logger.log(Level.INFO, "Opening folder {0}", logFolder.getPath());
        logFileDir.clear();
        platforms.clear();

        if (logFolder.isFolder()) {
            Enumeration<? extends FileObject> children = logFolder.getChildren(true);

            while (children.hasMoreElements()) {
                FileObject file = children.nextElement();

                if (file.getNameExt().endsWith(".log") || file.getNameExt().endsWith(".log.gz")) {
                    try {
                        LogFile logFile = new LogFile(FileUtil.toFile(file));

                        if (platformList.containsKey(logFile.getPlatform())) {
                            ArrayList<LogFile> platform = platformList.get(logFile.getPlatform());
                            platform.add(logFile);
                        } else {
                            ArrayList<LogFile> platform = new ArrayList<LogFile>();
                            platform.add(logFile);
                            platformList.put(logFile.getPlatform(), platform);
                            platforms.add(logFile.getPlatform());
                        }
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "Found malformed log file: {0}. Ignoring...", file.getName());
                    }
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
            ArrayList<LogFile> platform = new ArrayList<LogFile>();
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

    public ArrayList<LogFile> getFilesForPlatform(String platform) {
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
