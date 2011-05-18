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
import com.github.kayak.logging.input.LogInputTopComponent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
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

        this.setIconBaseWithExtension("org/freedesktop/tango/16x16/apps/accessories-text-editor.png");
    }

    @Override
    public Action[] getActions(boolean foo) {
        ArrayList<Action> actions = new ArrayList<Action>();
        
        actions.add(new OpenAction());
        actions.add(new DeleteAction());
        if(!logFile.getCompressed())
            actions.add(new CompressAction());
        
        if(!manager.getFavouries().contains(logFile))
            actions.add(new BookmarkAction());
            
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
                ArrayList<String> busses = logFile.getBusses();
                
                StringBuilder sb = new StringBuilder();
                
                for(int i=0;i<busses.size();i++) {
                    sb.append(busses.get(i));
                    if(i < (busses.size()-1)) {
                        sb.append(", ");
                    }
                }
                
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

        Property length = new PropertySupport.ReadOnly<String>("Length", String.class, "Length", "Length of the file in milliseconds") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return String.valueOf(logFile.getLength() / 1000) + "." + String.valueOf(logFile.getLength() % 1000);
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

    private class OpenAction extends AbstractAction {
        
        public OpenAction() {
            putValue (NAME, "Open");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LogInputTopComponent tc = new LogInputTopComponent();
            tc.setLogFile(logFile);
            tc.open();
            tc.requestActive();
        }
    };
    
    private class DeleteAction extends AbstractAction {
        
        public DeleteAction() {
            putValue (NAME, "Delete...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LogFile lf = getLookup().lookup (LogFile.class);
            
            if(lf != null) {
                int res = JOptionPane.showConfirmDialog(null, "", "Are you sure?", JOptionPane.YES_NO_OPTION);
                
                if(res == JOptionPane.YES_OPTION) {
                    File f = lf.getFile();
                    f.delete();
                    try {
                        destroy();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    LogFileManager.getGlobalLogFileManager().removeLogFile(lf);
                }
            }

        }
    };
    
    private class CompressAction extends AbstractAction {
        
        public CompressAction() {
            putValue (NAME, "Compress");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LogFile lf = getLookup().lookup(LogFile.class);

            if (lf != null) {
                File f = lf.getFile();

                try {

                    File newFile = new File(f.getAbsolutePath() + ".gz");
                    GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(newFile));
                    FileInputStream in = new FileInputStream(f);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();

                    out.finish();
                    out.close();
                    f.delete();
                    LogFileManager.getGlobalLogFileManager().removeLogFile(lf);
                    LogFileManager.getGlobalLogFileManager().addLogFile(new LogFile(newFile));
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Could not compress log file");
                }
            }
        }
    };
    
    private class BookmarkAction extends AbstractAction {
        
        public BookmarkAction() {
            putValue (NAME, "Bookmark");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LogFile lf = getLookup().lookup(LogFile.class);

            if (lf != null) {
                manager.addFavourite(lf);
            }
        }
    };

}
