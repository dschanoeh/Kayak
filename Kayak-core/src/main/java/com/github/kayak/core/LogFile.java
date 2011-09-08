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
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Represents an already existing log file. On creation the file is parsed and
 * the relevant content is available as properties.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFile {

    private static final Logger logger = Logger.getLogger(LogFile.class.getCanonicalName());

    private Boolean compressed;
    private File file;
    private InputStream inputStream;
    private String description;
    private String platform;
    private HashMap<String, String> deviceAlias;
    private long length;

    public static final Pattern platformPattern = Pattern.compile("[A-Z0-9_]+");
    public static final Pattern descriptionPattern = Pattern.compile("[a-zA-Z0-9\\s]+");
    public static final Pattern descriptionLinePattern = Pattern.compile("DESCRIPTION \"[a-zA-Z0-9\\s]+\"");
    public static final Pattern platformLinePattern = Pattern.compile("PLATFORM [A-Z0-9_]+");
    public static final Pattern deviceAliasLinePattern = Pattern.compile("DEVICE_ALIAS [A-Za-z0-9]+ [a-z0-9]{1,16}");

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

    public void setPlatform(String platform) throws FileNotFoundException, IOException {
        if(!platformPattern.matcher(platform).matches())
            throw new IllegalArgumentException("Platform must match " + platformPattern.pattern());

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
        if(!descriptionPattern.matcher(description).matches())
            throw new IllegalArgumentException("Description must match " + descriptionPattern.pattern());

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

    public LogFile(File file) throws FileNotFoundException, IOException {
        this.file = file;
        this.platform = "";
        this.description = "";
        deviceAlias = new HashMap<String, String>();
        String filename = file.getPath();

        if (filename.endsWith(".log.gz")) {
            compressed = true;
            inputStream = new GZIPInputStream(new FileInputStream(file));
        } else {
            compressed = false;
            inputStream = new FileInputStream(file);
        }

        parseHeader();
    }

    private void parseHeader() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while (true) {
                String line = reader.readLine();

                if(descriptionLinePattern.matcher(line).matches()) {
                    int start = line.indexOf('\"') + 1;
                    int stop = line.lastIndexOf("\"");
                    description = line.substring(start, stop);
                } else if(platformLinePattern.matcher(line).matches()) {
                    int start = line.indexOf(' ') + 1;
                    platform = line.substring(start);
                } else if (deviceAliasLinePattern.matcher(line).matches()) {
                    int start = line.indexOf(' ') + 1;
                    int stop = line.lastIndexOf(' ');
                    String alias = line.substring(start, stop);
                    String bus = line.substring(stop + 1);
                    deviceAlias.put(bus, alias);
                /*
                 * All lines that are not recognized and not pure whitespace cause
                 * the header parsing to abort.
                 */
                } else if (!line.matches("\\s")) {
                        if (description.equals("")) {
                            description = file.getName();
                        }
                        if (platform.equals("")) {
                            platform = "No platform";
                        }
                        break;
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "IOException while loading log file.", ex);
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not close reader.", ex);
            }
        }

        /* TODO: get length of file */
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogFile other = (LogFile) obj;
        if(other.getFileName().equals(getFileName()))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        return getFileName().hashCode();
    }

}
