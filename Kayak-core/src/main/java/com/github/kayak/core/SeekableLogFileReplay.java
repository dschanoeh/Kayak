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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A log file replay that can seek to any position in the log file. This works
 * only with log files that are not compressed.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class SeekableLogFileReplay {

    private static final Logger logger = Logger.getLogger(LogFileReplay.class.getCanonicalName());

    private LogFile logFile;
    private TimeSource timeSource;
    private Thread thread;
    private long currentTimestamp; /* time of the current frame in the log file */
    private Map<String, Bus> busses = new HashMap<String, Bus>();
    private boolean infiniteReplay;
    private BufferedLineReader reader;

    private long replayStartTime; /* Timesource time when the replay was started */
    private long in;
    private long out;
    private long[] index;
    private boolean indexCreated;
    private Thread indexCreationThread;

    private List<Command> commands = Collections.synchronizedList(new ArrayList<Command>());

    private static class Command {

        public static enum TYPE { SEEK, PLAY, STOP, PAUSE};

        private TYPE type;

        private long time;

        public Command(TYPE type) {
            this.type = type;
        }

        public TYPE getType() {
            return type;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

    }

    public boolean isIndexCreated() {
        return indexCreated;
    }

    private Runnable indexCreationRunnable = new Runnable() {
        @Override
        public void run() {

            BufferedLineReader reader = null;
            try {
                reader = new BufferedLineReader(logFile.getFile(), logFile.getStartPosition());


                for(long currentTime=logFile.getStartTime();currentTime<logFile.getStopTime();) {
                    String line = reader.readLine();
                    if(line == null)
                        break;

                    Frame f = Frame.fromLogFileNotation(line).getFrame();
                    if(f.getTimestamp() >= currentTime) {
                        int i=(int) ((currentTime-logFile.getStartTime())/1000000);
                        index[i] = reader.getPositionOfLastLine();
                        currentTime += 1000000;
                    }
                }
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, "File not found!", ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "IOException while creating index", ex);
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }

            indexCreated = true;

            logger.log(Level.INFO, "Index was created (size {3}) ({0} {1} {2} ... {4})",
                    new Object[] {index[0], index[1], index[2], index.length, index[index.length-1]});
        }
    };

    public long getIn() {
        return in - logFile.getStartTime();
    }

    public void setIn(long in) {
        this.in = logFile.getStartTime() + in;
    }

    public long getOut() {
        return out - logFile.getStartTime();
    }

    public void setOut(long out) {
        this.out = logFile.getStartTime() + out;
    }

    /**
     * The time difference between the first frame in the file and the last
     * frame
     */
    public long getLength() {
        return logFile.getLength();
    }

    /**
     * The time between the first frame and the current frame
     */
    public long getCurrentTime() {
        return currentTimestamp - logFile.getStartTime();
    }

    /**
     * True if the replay of the log file will be repeated infinitely
     */
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

    public Bus getBus(String name) {
        return busses.get(name);
    }

    /**
     * Create a new {@link LogFileReplay} with a specific {@link LogFile}.
     * @param logFile
     */
    public SeekableLogFileReplay(LogFile logFile) throws FileNotFoundException {
        this.logFile = logFile;

        try {
            reader = new BufferedLineReader(logFile.getFile(), logFile.getStartPosition());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        index = new long[(int) (logFile.getLength()/1000000)+1];
        indexCreationThread = new Thread(indexCreationRunnable);
        indexCreationThread.setName("LogFile index creation");
        indexCreationThread.setPriority(Thread.MIN_PRIORITY);
        indexCreationThread.start();

        in = logFile.getStartTime();
        out = logFile.getStopTime();
        currentTimestamp = in;

        logger.log(Level.INFO, "New log file replay. Length from {0} to {1}", new Object[]{logFile.getStartTime(), logFile.getStopTime()});
        logger.log(Level.INFO, "Start position: {0}", new Object[]{logFile.getStartPosition()});
    }



    /**
     *
     * @param time Time in microseconds
     * @return
     * @throws InterruptedException
     */
    private long findSeekPosition(long time) {
        if(time <= 0) {
            return logFile.getStartPosition();
        }

        if(!indexCreated) {
            return logFile.getStartPosition();
        }

        long i = index[(int) (time/1000000)];
        logger.log(Level.INFO, "Seek time {0} translates to position {1}",
                new Object[] { time, i});
        return i;
    }

    /**
     * Seek to a position in the log file relative to the first frame
     * @param time
     */
    public void seekTo(long time) {
        logger.log(Level.INFO, "Seeking to {0}", time);
        Command c = new Command(Command.TYPE.SEEK);
        c.setTime(time);
        commands.add(c);
        thread.interrupt();
    }

    private Frame.FrameBusNamePair readNextFrame() {
        try {
            String line = reader.readLine();
            if(line != null)
                return Frame.fromLogFileNotation(line);
            else
                return null;
        } catch(IOException ex) {
            return null;
        }
    }

    private Runnable myRunnable = new Runnable() {

        private Command getCommand() {
            if(commands.size()>0) {
                Command c = commands.get(0);
                commands.remove(c);
                return c;
            } else {
                return null;
            }
        }

        private void seekTo(long time) {
            long pos = findSeekPosition(time);
            if(pos > 0) {
                try {
                    reader.seek(pos);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Exception while seeking to beginning", ex);
                }
                replayStartTime = timeSource.getTime() - (time/1000);
            }
        }

        private static final int MODE_PLAY = 0;
        private static final int MODE_PAUSE = 1;
        private static final int MODE_STOP = 2;

        @Override
        public void run() {
            int mode = MODE_PLAY;
            replayStartTime = timeSource.getTime() - (getIn()/1000);


            while(true) {
                switch(mode) {
                    case MODE_PLAY:
                        while(true) {
                            Command c = getCommand();
                            if(c != null) {
                                if(c.getType() == Command.TYPE.STOP) {
                                    logger.log(Level.INFO, "Play->Stop");
                                    mode = MODE_STOP;
                                    break;
                                } else if(c.getType() == Command.TYPE.PAUSE) {
                                    logger.log(Level.INFO, "Play->Pause");
                                    mode = MODE_PAUSE;
                                    break;
                                }else if(c.getType() == Command.TYPE.SEEK) {
                                    logger.log(Level.INFO, "Play->Seek");
                                    seekTo(c.getTime());
                                }
                            }

                            /* try to read a frame */
                            Frame.FrameBusNamePair pair = readNextFrame();
                            if(pair != null) {
                                Frame f = pair.getFrame();
                                long timestamp = f.getTimestamp();
                                if(timestamp > out) { /* End position was reached */
                                    if(infiniteReplay) {
                                        logger.log(Level.INFO, "Reached the end of the log file. Seeking to beginning.");
                                        seekTo(getIn());
                                        continue;
                                    } else {
                                        return;
                                    }
                                }

                                String busName = pair.getBusName();
                                Bus bus = busses.get(busName);

                                if(bus != null) {
                                    /*
                                     * Sleep time in microseconds
                                     * (relative time in log file) - (relative time since replay start)
                                     */
                                    long timeToWait = (timestamp-logFile.getStartTime()) - (timeSource.getTime()-replayStartTime)*1000;

                                    try {
                                        if(timeToWait > 10000)
                                            Thread.sleep(timeToWait / 1000);
                                        currentTimestamp = timestamp;
                                        bus.sendFrame(f);
                                    } catch (InterruptedException ex) {
                                        /* Command will be checked in next loop */
                                    }
                                }
                            } else {
                                if(infiniteReplay) {
                                    logger.log(Level.INFO, "Reached the end of the log file. Seeking to beginning.");
                                    seekTo(getIn());
                                }
                            }
                        }
                        break;
                    case MODE_PAUSE:
                        Command c;
                        while(true) {
                            try {
                            Thread.sleep(100);
                            } catch (InterruptedException ex) {

                            }
                            c = getCommand();
                            if(c != null)
                                break;
                        }

                        switch(c.getType()) {
                            case PLAY:
                                logger.log(Level.INFO, "Pause->Play");
                                mode = MODE_PLAY;
                                break;
                            case SEEK:
                                seekTo(c.getTime());
                                break;
                        }
                        break;
                    case MODE_STOP:
                        logger.log(Level.INFO, "Stopped");
                        return;
                }
            }
        }
    };

    private TimeEventReceiver timeEventReceiver = new TimeEventReceiver() {

        @Override
        public void paused() {
            commands.add(new Command(Command.TYPE.PAUSE));
            thread.interrupt();
        }

        @Override
        public void played() {
            /* thread was paused */
            if(thread != null && thread.isAlive()) {
                Command c = new Command(Command.TYPE.PLAY);
                commands.add(c);
                thread.interrupt();

            /* thread was stopped */
            } else {
                replayStartTime = timeSource.getTime();
                thread = new Thread(myRunnable);
                thread.start();
            }
        }

        @Override
        public void stopped() {
            if(thread != null && thread.isAlive()) {
                commands.add(new Command(Command.TYPE.STOP));
                thread.interrupt();
                Command c = new Command(Command.TYPE.SEEK);
                c.setTime(getIn());
                commands.add(c);
            }
        }
    };
}
