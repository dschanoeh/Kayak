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
package com.github.kayak.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFile {
    
    private static final Logger logger = Logger.getLogger(LogFile.class.getCanonicalName());

    private Boolean compressed;
    private File file;
    private InputStream inputStream;
    private boolean write;
    private String description;
    private String platform;
    private HashMap<String, String> deviceAlias;
    private long length;

    public long getLength() {
        return length;
    }

    public long getSize() {
        return file.length();
    }

    public File getFile() {
        return file;
    }

    public String getAlias(String s) {
        return deviceAlias.get(s);
    }

    public ArrayList<String> getBusses() {
        ArrayList<String> busNames = new ArrayList<String>();
        Set<String> keys = deviceAlias.keySet();

        for (String bus : keys) {
            busNames.add(deviceAlias.get(bus));
        }

        return busNames;

    }

    public String getDescription() {
        return description;
    }

    public String getPlatform() {
        return platform;
    }

    public Boolean getCompressed() {
        return compressed;
    }

    public String getFileName() {
        return file.getName();
    }

    public Boolean hasWriteAccess() {
        return write;
    }

    public Boolean hasReadAccess() {
        return !write;
    }
    
    public void setPlatform(String platform) throws FileNotFoundException, IOException {
        
        if(!platform.matches("[A-Z0-9_]+"))
            throw new IllegalArgumentException("Platform must match [A-Z0-9_]+");

        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        BufferedReader br;
        PrintWriter pw;
        
        if(compressed) {
            GZIPInputStream zipStream = new GZIPInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(zipStream));
            GZIPOutputStream outStream = new GZIPOutputStream(new FileOutputStream(tempFile));
            pw = new PrintWriter(outStream);
        } else {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            pw = new PrintWriter(new FileWriter(tempFile));
        }

        String line = null;
        boolean written = false;

        while ((line = br.readLine()) != null) {
            /* If line is found overwrite it */
            if (!written && line.startsWith(("PLATFORM"))) {
                pw.println("PLATFORM " + platform);
                written = true;
            /* If header has no such field add it */
            } else if(!written && line.startsWith("(")) {
                pw.println("PLATFORM " + platform);
                pw.println(line);
                written = true;
            /* Write all other header lines */
            } else {
                pw.println(line);
                pw.flush();
            }
        }
        
        pw.close();
        br.close();

        if (!file.delete()) {
            logger.log(Level.WARNING, "Could not delete old file");
            return;
        }

        if (!tempFile.renameTo(file)) {
            logger.log(Level.WARNING, "Could not rename new file to old filename");
        }
        
        this.platform = platform;
    }
    
    public void setDescription(String description) throws FileNotFoundException, IOException {
        
        if(!description.matches("[a-zA-Z0-9\\s]+"))
            throw new IllegalArgumentException("Description must match [a-zA-Z0-9\\s]+");

        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        BufferedReader br;
        PrintWriter pw;
        
        if(compressed) {
            GZIPInputStream zipStream = new GZIPInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(zipStream));
            GZIPOutputStream outStream = new GZIPOutputStream(new FileOutputStream(tempFile));
            pw = new PrintWriter(outStream);
        } else {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            pw = new PrintWriter(new FileWriter(tempFile));
        }

        String line = null;
        boolean written = false;

        while ((line = br.readLine()) != null) {
            /* If line is found overwrite it */
            if (!written && line.startsWith(("DESCRIPTION"))) {
                pw.println("DESCRIPTION \"" + description + "\"");
                written = true;
            /* If header has no such field add it */
            } else if(!written && line.startsWith("(")) {
                pw.println("DESCRIPTION \"" + description + "\"");
                pw.println(line);
                written = true;
            /* Write all other header lines */
            } else {
                pw.println(line);
                pw.flush();
            }
        }
        
        pw.close();
        br.close();

        if (!file.delete()) {
            logger.log(Level.WARNING, "Could not delete old file");
            return;
        }

        if (!tempFile.renameTo(file)) {
            logger.log(Level.WARNING, "Could not rename new file to old filename");
        }
        
        this.description = description;
    }

    private LogFile(File file, Boolean compressed, Boolean write) throws FileNotFoundException, IOException {
        this.file = file;
        this.compressed = compressed;
        this.write = write;
        this.platform = "";
        this.description = "";
        deviceAlias = new HashMap<String, String>();

        if (!write) {
            if (compressed) {
                inputStream = new GZIPInputStream(new FileInputStream(file));
            } else {
                inputStream = new FileInputStream(file);
            }
        }

        parseHeader();
    }

    public static LogFile fromFile(File file) {
        if (!file.canRead()) {
            return null;
        }

        String filename = file.getPath();
        LogFile logFile;

        if (filename.endsWith(".log.gz")) {
            try {
                logFile = new LogFile(file, true, false);
            } catch (Exception ex) {
                return null;
            }
        } else if (filename.endsWith(".log")) {
            try {
                logFile = new LogFile(file, false, false);
            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }

        return logFile;
    }

    public static LogFile create(String filename, boolean gzipped) {
        LogFile logFile;
        try {
            logFile = new LogFile(null, gzipped, true);
        } catch (Exception ex) {
            return null;
        }

        return logFile;
    }

    private void parseHeader() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while (true) {

                String line = reader.readLine();

                if (line.startsWith("DESCRIPTION")) {
                    if (line.matches("DESCRIPTION \"[a-zA-Z0-9\\s]+\"")) {
                        int start = line.indexOf('\"') + 1;
                        int stop = line.lastIndexOf("\"");
                        description = line.substring(start, stop);
                    }
                } else if (line.startsWith("PLATFORM")) {
                    if(line.matches("PLATFORM [A-Z0-9_]+")) {
                        int start = line.indexOf(' ') + 1;
                        platform = line.substring(start);
                    }
                } else if (line.startsWith("DEVICE_ALIAS")) {
                    if (line.matches("DEVICE_ALIAS [A-Za-z0-9]+ [a-z0-9]{1,16}")) {
                        int start = line.indexOf(' ') + 1;
                        int stop = line.lastIndexOf(' ');
                        String alias = line.substring(start, stop);
                        String bus = line.substring(stop + 1);
                        deviceAlias.put(bus, alias);
                    }
                    /*
                     * All lines that are not recognized and not pure whitespace cause
                     * the header parsing to abort.
                     */
                } else {
                    if (!line.matches("\\s")) {
                        if (description.equals("")) {
                            description = file.getName();
                        }
                        if (platform.equals("")) {
                            platform = "No platform";
                        }
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            return;
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
            }
            ;
        }

        /* TODO: get length of file */
    }
}
