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
    private long startTime; /* time of the first frame in the log file */
    private long currentTimestamp; /* time of the current frame in the log file */
    private Map<String, Bus> busses = new HashMap<String, Bus>();
    private boolean infiniteReplay;
    private final RandomAccessFile file;
    private long stopTime; /* time of the last frame in the file */
    private long startPosition;
    private long replayStartTime;
    private long in;
    private long out;

    private List<Command> commands = Collections.synchronizedList(new ArrayList<Command>());

    private static class Command {

        public static enum TYPE { SEEK, PLAY, STOP, PAUSE};

        private TYPE type;

        private long position;

        public Command(TYPE type) {
            this.type = type;
        }

        public TYPE getType() {
            return type;
        }

        public long getPosition() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }

    }

    public long getIn() {
        return in - startTime;
    }

    public void setIn(long in) {
        this.in = startTime + in;
    }

    public long getOut() {
        return out - startTime;
    }

    public void setOut(long out) {
        this.out = startTime + out;
    }

    /**
     * The time difference between the first frame in the file and the last
     * frame
     */
    public long getLength() {
        return stopTime - startTime;
    }

    /**
     * The time between the first frame and the current frame
     */
    public long getCurrentTime() {
        return currentTimestamp - startTime;
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

    /**
     * Create a new {@link LogFileReplay} with a specific {@link LogFile}.
     * @param logFile
     */
    public SeekableLogFileReplay(LogFile logFile) throws FileNotFoundException {
        this.logFile = logFile;

        file = new RandomAccessFile(logFile.getFile(), "r");

        findStopPosition();
        findStartPosition();

        logger.log(Level.INFO, "New log file replay. Length from {0} to {1}", new Object[]{startTime, stopTime});
        try {
            logger.log(Level.INFO, "Positions in file are {0} and {1}", new Object[]{startPosition, file.length()});
        } catch (IOException ex) {
            Logger.getLogger(SeekableLogFileReplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Seeks the file to the position of the first frame and returns
     * this position
     */
    private long findStartPosition() {
        try {
            file.seek(0);

            while(true) {
                long posBefore = file.getFilePointer();
                String line = file.readLine();
                if(line.startsWith("(")) {
                    Frame f = Frame.fromLogFileNotation(line).getFrame();
                    startTime = f.getTimestamp();
                    currentTimestamp = startTime;
                    in = startTime;
                    startPosition = posBefore;
                    file.seek(posBefore);
                    return posBefore;
                }
            }
        } catch(IOException ex) {
                return -1;
        }

    }

    /**
     * Seeks the file to the position of the last frame and returns
     * this position
     */
    private long findStopPosition() {
        try {
            for(long checkPos = file.length() - 10;checkPos>0;checkPos--) {
                file.seek(checkPos);

                String line = file.readLine();

                if(line != null) {
                    Frame.FrameBusNamePair pair = Frame.fromLogFileNotation(line);

                    if(pair != null) {
                        file.seek(checkPos);
                        stopTime = pair.getFrame().getTimestamp();
                        out = stopTime;
                        return checkPos;
                    }
                }
            }
        } catch(IOException ex) {
                return -1;
        }
        return -1;
    }

    /**
     * Seek file to the line nearest to position. makes sure that it seeks to
     * the beginning of a line and not before the start of the file or after the
     * end of the file
     */
    private void safeFileSeek(long position, RandomAccessFile f) throws IOException {

        if(position > startPosition) {
            if(position < (f.length() - 150)) { /* totally safe */
                f.seek(position);
                f.readLine();
                return;
            } else { /* find a valid line at position or go back bytewise */
                for(long checkPos = position;checkPos>0;checkPos--) {
                    f.seek(checkPos);

                    String line = f.readLine();

                    if(line != null) {
                        Frame.FrameBusNamePair pair = Frame.fromLogFileNotation(line);

                        if(pair != null) {
                            f.seek(checkPos);
                            return;
                        }
                    }
                }
            }
        } else { /* first possible position */
            file.seek(startPosition);
        }
    }

    private long findSeekPosition(long time) {
        if(time <= 0) {
            return startPosition;
        }

        try {
            RandomAccessFile newFile = new RandomAccessFile(logFile.getFile(), "r");

            if(time >= 0 && time <= getLength()) {
                time += startTime;

                /* The position of the first frame after 'time' must be found in
                 * the file. The position is completely unclear.
                 * Start with a guess position based on relative time and go
                 * forward or backward from this point.
                 */
                long guessPosition = newFile.length() * (time - startTime) / (stopTime- startTime);

                if(guessPosition <= startPosition)
                    safeFileSeek(startPosition, newFile);
                else if(guessPosition >= newFile.length())
                    findStopPosition();
                else
                    safeFileSeek(guessPosition, newFile);

                while(true) {
                    long positionBeforeRead = newFile.getFilePointer();
                    Frame f = Frame.fromLogFileNotation(newFile.readLine()).getFrame();

                    /* we have to go forward -> read next line */
                    if(f.getTimestamp() < time) {
                        continue;
                    /* we could either be to far in the file or at the right
                     * position. Try to find previous frame to check if we have
                     * to go backward.
                     */
                    } else {
                        for(long previousFramePosition = positionBeforeRead - 10; previousFramePosition > 0;previousFramePosition--) {

                            newFile.seek(previousFramePosition);
                            String line = newFile.readLine();
                            Frame previousFrame = null;
                            if(line != null) {
                                Frame.FrameBusNamePair pair = Frame.fromLogFileNotation(line);
                                if(pair != null)
                                    previousFrame = pair.getFrame();
                            }

                            /* we are at the right position \o/ */
                            if(previousFrame != null) {
                                if(previousFrame.getTimestamp() < time) {
                                    newFile.close();
                                    return positionBeforeRead;
                                /* we have to go backward -> seek back some bytes */
                                } else {
                                    newFile.seek(previousFramePosition - 200);
                                    newFile.readLine(); /* make sure we are at the start of a line */
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            newFile.close();
        } catch(FileNotFoundException ex ) {
            logger.log(Level.WARNING, "Could not find seek position", ex);
        } catch(IOException ex) {
            logger.log(Level.WARNING, "Could not find seek position", ex);
        }

        return -1;
    }

    /**
     * Seek to a position in the log file relative to the first frame
     * @param time
     */
    public void seekTo(long time) {
        long pos = findSeekPosition(time);

        if(pos != -1) {
            Command c = new Command(Command.TYPE.SEEK);
            c.setPosition(pos);
            commands.add(c);
        }
    }

    private Frame.FrameBusNamePair readNextFrame() {
        try {
            String line = file.readLine();
            if(line != null)
                return Frame.fromLogFileNotation(line);
            else
                return null;
        } catch(IOException ex) {
            return null;
        }
    }

    private Runnable myRunnable = new Runnable() {

        private boolean checkCommands() {
            if(commands.size()>0) {
                Command c = commands.get(0);
                commands.remove(c);
                switch(c.getType()) {
                    case SEEK:
                       try {
                           file.seek(c.getPosition());
                       } catch (IOException ex) {
                           Logger.getLogger(SeekableLogFileReplay.class.getName()).log(Level.SEVERE, null, ex);
                       }
                       return false;
                    case STOP:
                        return true;
                    case PAUSE:
                        while(true) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex1) {
                                if (commands.size() > 0) {
                                    for(Command c1 : commands) {
                                        if(c1.getType() == Command.TYPE.PLAY) {
                                            commands.remove(c1);
                                            return false;
                                        } else if(c1.getType() == Command.TYPE.STOP) {
                                            commands.remove(c1);
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                            }
                        }
                    default:
                       break;
                }
            }
            return false;
        }

        private void seekToIn() {
            long pos = findSeekPosition(getIn());
            if(pos != -1) {
                try {
                    file.seek(pos);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Exception while seeking to beginning", ex);
                }
                replayStartTime = timeSource.getTime();
            }
        }

        @Override
        public void run() {
            while (true) {
                /* try to read a frame */
                Frame.FrameBusNamePair pair = readNextFrame();
                if(pair != null) {
                    Frame f = pair.getFrame();
                    long timestamp = f.getTimestamp();
                    if(timestamp > out) {
                        if(infiniteReplay) {
                            logger.log(Level.INFO, "Reached the end of the log file. Seeking to beginning.");
                            seekToIn();
                            continue;
                        } else {
                            return;
                        }
                    }

                    String busName = pair.getBusName();
                    Bus bus = busses.get(busName);

                    if(bus != null) {
                        /*                 (relative time in log file) - (relative time since replay start) */
                        long timeToWait = ((timestamp-in)/1000) - (timeSource.getTime()-replayStartTime);

                        /* if timeToWait is <0 we are to late. if it is >0 we have to wait. This only makes sense if
                         * it is more than a few ms.
                         */
                        if (timeToWait >= 10) {
                            try {
                                Thread.sleep(timeToWait);
                            } catch (InterruptedException ex) {
                                if(checkCommands())
                                    return;
                            }
                        }
                        currentTimestamp = timestamp;
                        bus.sendFrame(f);
                    }

                } else {
                    /* are we at the end of the file? */
                    boolean eof = false;
                    synchronized(file) {
                        try {
                            if(file.getFilePointer() == file.length()) {
                                eof = true;
                            }
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, "IOException while checking for EOF", ex);
                        }
                    }
                    if(eof) {
                        if(infiniteReplay) {
                            logger.log(Level.INFO, "Reached the end of the log file. Seeking to beginning.");
                            seekToIn();
                            continue;
                        } else {
                            return;
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {

                        }
                    }
                }

                if(checkCommands())
                    return;
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
                Command c = new Command(Command.TYPE.SEEK);
                c.setPosition(getIn());
                commands.add(c);
                commands.add(new Command(Command.TYPE.STOP));
                thread.interrupt();
            }
        }
    };
}
