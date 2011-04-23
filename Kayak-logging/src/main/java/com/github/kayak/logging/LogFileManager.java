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
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFileManager {
    
    private static final Logger logger = Logger.getLogger(LogFileManager.class.getCanonicalName());
    private static LogFileManager manager;

    private HashMap<String,ArrayList<LogFile>> platformList;
    private TreeSet<String> platforms;

    public TreeSet<String> getPlatforms() {
        return platforms;
    }

    private ArrayList<LogFile> logFileDir;
    private FileObject logFolder;

    public String getLogFolder() {
        return logFolder.getPath();
    }

    public LogFileManager() {
        String homeFolder = System.getProperty("user.home");
        logFolder = FileUtil.toFileObject(new File(NbPreferences.forModule(LogFileManager.class).get("Log file directory", homeFolder + "/kayak/log/")));

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
                        LogFile logFile = LogFile.fromFile(FileUtil.toFile(file));

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

    public ArrayList<LogFile> getFilesForPlatform(String platform) {
        if(platforms.contains(platform)) {
            return platformList.get(platform);
        } else
            return null;
    }

}
