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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFile {

    private Boolean compressed;
    private File file;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean write;
    private String description;
    private String platform;
    private HashMap<String,String> deviceAlias;

    public ArrayList<String> getBusses() {
        ArrayList<String> busNames = new ArrayList<String>();
        Set<String> keys = deviceAlias.keySet();

        for(String bus : keys) {
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

    public InputStream getInputStream() {
        if(!write)
            return inputStream;

        return null;
    }

    public OutputStream getOutputStream() {
        if(write)
            return outputStream;

        return null;
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
        } else {
            if(compressed) {
                outputStream = new GZIPOutputStream(new FileOutputStream(file));
            } else {
                outputStream = new FileOutputStream(file);
            }
        }

        parseHeader();
    }

    public static LogFile fromFile(File file) {
        if(!file.canRead())
            return null;

        String filename = file.getPath();
        LogFile logFile;

        if(filename.endsWith(".log.gz")) {
            try {
                logFile = new LogFile(file, true, false);
            } catch(Exception ex) {
                return null;
            }
        } else if(filename.endsWith(".log")) {
            try {
                logFile = new LogFile(file, false, false);
            } catch(Exception ex) {
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
        } catch(Exception ex) {
            return null;
        }

        return logFile;
    }

    private void parseHeader() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream()));

        while(true) {
            try {
                String line = reader.readLine();

                if(line.startsWith("DESCRIPTION")) {
                    if(line.matches("DESCRIPTION \"[a-zA-Z0-9\\s]+\"")) {
                        int start = line.indexOf('\"') + 1;
                        int stop = line.lastIndexOf("\"");
                        description = line.substring(start, stop);
                    }
                } else if(line.startsWith("PLATFORM")) {
                    //if(line.matches("PLATFORM \"[A-Z0-9]+\"")) {
                        int start = line.indexOf(' ') + 1;
                        platform = line.substring(start);
                   // }
                } else if(line.startsWith("DEVICE_ALIAS")) {
                    if(line.matches("DEVICE_ALIAS [A-Za-z0-9]+ [a-z0-9]{1,16}")) {
                        int start = line.indexOf(' ') + 1;
                        int stop = line.lastIndexOf(' ');
                        String alias = line.substring(start, stop);
                        String bus = line.substring(stop+1);
                        deviceAlias.put(bus, alias);
                    }
                /*
                 * All lines that are not recognized and not pure whitespace cause
                 * the header parsing to abort.
                 */
                } else {
                    if(!line.matches("\\s")) {
                        if(description.equals(""))
                            description = file.getName();
                        if(platform.equals(""))
                            platform = "No platform";
                        return;
                    }
                }
            } catch (IOException ex) {
                return;
            }

        }
    }

}
