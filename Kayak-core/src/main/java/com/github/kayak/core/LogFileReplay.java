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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFileReplay {

    private static final Logger logger = Logger.getLogger(LogFileReplay.class.getCanonicalName());

    private LogFile logFile;
    private TimeSource timeSource;
    private TimeSource.Mode mode;
    private BufferedReader reader;
    private Thread thread;
    private long timeOffset; /* time of the first frame in the log file */
    private long startTime; /* time when the replay was started */
    private HashMap<String, Bus> busses;
    private boolean infiniteReplay;

    public boolean isInfiniteReplay() {
        return infiniteReplay;
    }

    public void setInfiniteReplay(boolean infiniteReplay) {
        this.infiniteReplay = infiniteReplay;
    }

    public TimeSource getTimeSource() {
        return timeSource;
    }

    public void setTimeSource(TimeSource source) {
        if (this.timeSource != null) {
            this.timeSource.deregister(timeEventReceiver);
        }
        this.timeSource = source;
        this.timeSource.register(timeEventReceiver);
        this.mode = timeSource.getMode();

    }

    /**
     * Connect a bus to the {@link LogFileReplay}. Every log entry with 'name'
     * will be sent to the corresponding bus.
     * @param name Name in the log file
     * @param bus Bus that will be connected to this name
     */
    public void setBus(String name, Bus bus) {
        busses.put(name, bus);
    }

    /**
     * Create a new {@link LogFileReplay} with a specific {@link LogFile}.
     * @param logFile
     */
    public LogFileReplay(LogFile logFile) {
        this.logFile = logFile;

        busses = new HashMap<String, Bus>();
        for (String b : logFile.getBusses()) {
            busses.put(b, null);
        }
    }

    private void seekToBeginning() {
        logger.log(Level.INFO, "Seeking to begin of file");
        try {
            if(reader != null)
                reader.close();
            
            InputStream inputStream;
            if (logFile.getCompressed()) {
                inputStream = new GZIPInputStream(new FileInputStream(logFile.getFile()));
            } else {
                inputStream = new FileInputStream(logFile.getFile());
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            /* Skip header */
            reader.mark(1024);
            while (true) {
                String line = reader.readLine();
                if (line != null && line.startsWith("(")) {
                    String[] cols = line.split("\\s");
                    timeOffset = (long) (Double.parseDouble((cols[0].substring(1, cols[0].length() - 1))) * 1000);
                    reader.reset();
                    break;
                }
                reader.mark(1024);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Exception while seeking to begin of file", ex);
        }
        
        startTime = timeSource.getTime();
    }

    private Runnable myRunnable = new Runnable() {

        private boolean checkMode() {
            if (mode == mode.STOP) {
                return true;
            } else if (mode == mode.PAUSE) {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex1) {
                        if (mode == mode.PLAY) {
                            return false;
                        } else if(mode == mode.STOP) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (reader.ready()) {
                        if(checkMode())
                            return;
                        String line = reader.readLine();
                        if (line.startsWith("(")) {
                            String[] cols = line.split("\\s");

                            /* check if we have a bus connected for this recorded bus */
                            Bus bus = busses.get(logFile.getAlias(cols[1]));

                            if (bus == null) {
                                continue;
                            }
                            long msecs = (long) (Double.parseDouble((cols[0].substring(1, cols[0].length() - 1))) * 1000) - timeOffset;

                            String[] data = cols[2].split("#");
                            int identifier = Integer.parseInt(data[0], 16);

                            byte[] message = Util.hexStringToByteArray(data[1]);

                            Frame frame = new Frame(identifier, message);

                            long timeToWait = msecs - (timeSource.getTime() - startTime);

                            /* if timeToWait is <0 we are to late. if it is >0 we have to wait. This only makes sense if
                             * it is more than a few ms.
                             */
                            if (timeToWait >= 10) {
                                try {
                                    Thread.sleep(timeToWait);
                                } catch (InterruptedException ex) {
                                    if(checkMode())
                                        return;
                                }
                            }

                            bus.sendFrame(frame);
                        } else if(line.startsWith("EVENT")) {
                            String[] cols = line.split("\\s");
                            
                            EventFrame ev;
                            Bus bus = null;
                            
                            if(cols[1].startsWith("(")) { /* timestamp */
                                if(cols[2].startsWith("\"")) { /* timestamp and bus name */
                                    ev = new EventFrame(cols[3].substring(1, cols[3].length()-1));
                                    bus = busses.get(logFile.getAlias(cols[2]));
                                } else { /* no bus name */
                                    ev = new EventFrame(cols[2].substring(1, cols[2].length()-1));
                                }
                                
                                long msecs = (long) (Double.parseDouble((cols[1].substring(1, cols[1].length() - 1))) * 1000) - timeOffset;
                                ev.setTimestamp(msecs);
                                long timeToWait = msecs - (timeSource.getTime() - startTime);
                            
                                if (timeToWait >= 10) {
                                    try {
                                        Thread.sleep(timeToWait);
                                    } catch (InterruptedException ex) {
                                        if(checkMode())
                                            return;
                                    }
                                }
                            } else { /* no timestamp */
                                if(cols[2].startsWith("\"")) { /* bus name */
                                    bus = busses.get(logFile.getAlias(cols[2]));
                                    ev = new EventFrame(cols[2].substring(1, cols[2].length()-1));
                                } else { /* no bus name */
                                    ev = new EventFrame(cols[1].substring(1, cols[1].length()-1));
                                }
                            }
                            
                            if (bus == null) {
                                Set<String> keys = busses.keySet();
                                for(String key : keys) {
                                    busses.get(key).sendEventFrame(ev);
                                }
                            }
                            
                            bus.sendEventFrame(ev);
                        }
                    } else {
                        if (infiniteReplay) {
                            EventFrame ev = new EventFrame("Seeking to beginning");
                            ev.setTimestamp(timeOffset);
                            
                            Set<String> keys = busses.keySet();
                            for(String key : keys) {
                                busses.get(key).sendEventFrame(ev);
                            }
                                
                            seekToBeginning();
                            continue;
                        } else {
                            return;
                        }
                    }


                } catch (IOException ex) {
                    Logger.getLogger(LogFileReplay.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    };

    private TimeEventReceiver timeEventReceiver = new TimeEventReceiver() {

        @Override
        public void paused() {
            mode = mode.PAUSE;
            thread.interrupt();
        }

        @Override
        public void played() {
            if (mode == mode.STOP) {
                seekToBeginning();
            }

            if (thread == null || !thread.isAlive()) {
                thread = new Thread(myRunnable);
                thread.start();
            } else {
                thread.interrupt();
            }

            mode = mode.PLAY;
        }

        @Override
        public void stopped() {
            mode = mode.STOP;
            if(thread != null && thread.isAlive())
                thread.interrupt();
        }
    };
}
