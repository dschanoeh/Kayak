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
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFileNode extends AbstractNode {

    private static final Logger logger = Logger.getLogger(LogFileNode.class.getCanonicalName());
    private LogFile logFile;
    private static final LogFileManager manager = LogFileManager.getGlobalLogFileManager();

    private void setText(String text) {
        this.setDisplayName(text);
    }

    public LogFileNode(LogFile logFile) {
        super(Children.LEAF, Lookups.singleton(logFile));
        this.logFile = logFile;
        this.setDisplayName(logFile.getDescription());
        this.setShortDescription(logFile.getFileName());

        this.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/apps/accessories-text-editor.png");
    }

    @Override
    public Action[] getActions(boolean foo) {
        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new OpenLogFileAction(logFile));
        actions.add(new DeleteLogFileAction(logFile));
        if(!logFile.getCompressed())
            actions.add(new CompressLogFileAction(logFile));

        if(!manager.getFavouries().contains(logFile))
            actions.add(new BookmarkLogFileAction(logFile));

        return actions.toArray(new Action[0]);
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property platform = new PropertySupport.ReadWrite<String>("Platform", String.class, "Platform", "Platform that was specified in the log file") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getPlatform();
            }

            @Override
            public void setValue(String t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                try {
                    LogFileManager.getGlobalLogFileManager().removeLogFile(logFile);
                    logFile.setPlatform(t);
                    LogFileManager.getGlobalLogFileManager().addLogFile(logFile);
                } catch(IllegalArgumentException ex) {
                    throw(ex);
                } catch(Exception ex) {
                    logger.log(Level.WARNING, "Could not change value of log file", ex);
                }
            }

        };

        Property fileName = new PropertySupport.ReadOnly<String>("File name", String.class, "File name", "Full file name of the log file") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getFileName();
            }

        };

        Property compressed = new PropertySupport.ReadOnly<Boolean>("Is compressed", Boolean.class, "Is compressed", "Indicates if the file was gzipped") {

            @Override
            public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getCompressed();
            }

        };

        Property description = new PropertySupport.ReadWrite<String>("Description", String.class, "Description", "Description that was defined in the log file") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getDescription();
            }

            @Override
            public void setValue(String t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                try {
                    logFile.setDescription(t);
                    setText(t);
                } catch(IllegalArgumentException ex) {
                    throw(ex);
                } catch(Exception ex) {
                    logger.log(Level.WARNING, "Could not change value of log file", ex);
                }
            }

        };

        Property busses = new PropertySupport.ReadOnly<String>("Busses", String.class, "Busses", "Busses that were logged into this file") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                StringBuilder sb = new StringBuilder();

                for(String bus : logFile.getBusses()) {
                    String alias = logFile.getAlias(bus);
                    sb.append(bus);
                    if(alias != null && !alias.equals("")) {
                        sb.append(" (");
                        sb.append(alias);
                        sb.append(")");
                    }
                    sb.append(", ");
                }

                sb.setLength(sb.length()-2);
                return sb.toString();
            }

        };

        Property size = new PropertySupport.ReadOnly<String>("Size", String.class, "Size", "Size of the file") {

            private final double BASE = 1024, KB = BASE, MB = KB * BASE, GB = MB * BASE;
            private final DecimalFormat df = new DecimalFormat("#.##");

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                long size = logFile.getSize();

                if (size >= GB) {
                    return df.format(size / GB) + " GB";
                }
                if (size >= MB) {
                    return df.format(size / MB) + " MB";
                }
                if (size >= KB) {
                    return df.format(size / KB) + " KB";
                }
                return "" + (int) size + " bytes";
            }
        };

        Property length = new PropertySupport.ReadOnly<String>("Length", String.class, "Length", "Length of the file in hours, minutes, seconds") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                long milliseconds = logFile.getLength() / 1000;

                int seconds = (int) (milliseconds / 1000) % 60;
                int minutes = (int) ((milliseconds / (1000*60)) % 60);
                int hours   = (int) ((milliseconds / (1000*60*60)));

                return String.format("%d:%02d:%02d", hours, minutes, seconds);
            }

        };

        Property edited = new PropertySupport.ReadOnly<String>("Edited", String.class, "Edited", "Time and date of the last file edit") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                Date d = new Date(logFile.getFile().lastModified());
                return DateFormat.getDateTimeInstance().format(d);
            }

        };

        set.put(platform);
        set.put(description);
        set.put(fileName);
        set.put(busses);
        set.put(compressed);
        set.put(size);
        set.put(length);
        set.put(edited);

        s.put(set);

        return s;
    }
}
