/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author dschanoeh
 */
public class LogFileReplay {

    private static final Logger logger = Logger.getLogger(LogFileReplay.class.getCanonicalName());

    private LogFile logFile;
    private TimeSource timeSource;
    private TimeSource.Mode mode;
    private BufferedReader reader;
    private Thread thread;
    private boolean loop;
    private long timeOffset;
    private HashMap<String, Bus> busses;

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

    public void setBus(String name, Bus bus) {
        busses.put(name, bus);
    }

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
            if (reader == null) {
                InputStream inputStream;
                if (logFile.getCompressed()) {
                    inputStream = new GZIPInputStream(new FileInputStream(logFile.getFile()));
                } else {
                    inputStream = new FileInputStream(logFile.getFile());
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
            }

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
        }
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

                            long timeToWait = msecs - (timeSource.getTime());

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
                        }
                    } else {
                        if (loop) {
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

            if (thread == null) {
                thread = new Thread(myRunnable);
            }

            if(!thread.isAlive())
                thread.start();

            mode = mode.PLAY;
        }

        @Override
        public void stopped() {
            mode = mode.STOP;
            thread.interrupt();
        }
    };
}
