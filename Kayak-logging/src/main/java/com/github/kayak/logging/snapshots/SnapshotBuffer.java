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
package com.github.kayak.logging.snapshots;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameListener;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.TimeSource;
import com.github.kayak.logging.options.Options;
import com.github.kayak.ui.projects.Project;
import java.io.IOException;
import com.github.kayak.ui.projects.ProjectChangeListener;
import com.github.kayak.ui.projects.ProjectManagementListener;
import com.github.kayak.ui.projects.ProjectManager;
import com.github.kayak.ui.time.TimeSourceManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * A SnapshotBuffer can be connected to a {@link Bus} and then buffers
 * all {@link Frame}s that are on the bus. Therefore it adds a RAW
 * subscription. After receiving Frames the Buffer can be connected to
 * a different (or the same) bus to replay the frames.
 * The depth of the buffer may be set and is per default 5 seconds.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class SnapshotBuffer {

    private static final Logger logger = Logger.getLogger(SnapshotBuffer.class.getCanonicalName());
    private static final Calendar cal = Calendar.getInstance();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
    private static final int depth = Options.getSnapshotBufferDepth();
    private final Set<Frame> frames = new TreeSet<Frame>(new Frame.TimestampComparator());
    private Thread cleanupThread;
    private int stopTimeout = 0;
    private boolean stopRequest = false;
    private final Map<Bus, Subscription> subscriptions = Collections.synchronizedMap(new HashMap<Bus, Subscription>());
    private Project currentProject;
    private boolean isBuffering = false;
    private ProjectManagementListener managementListener = new ProjectManagementListener() {

        @Override
        public void projectsUpdated() {
        }

        @Override
        public void openProjectChanged(Project p) {
            logger.log(Level.INFO, "Switching snapshot buffer to new project");
            if (currentProject != null) {
                currentProject.removeProjectChangeListener(projectListener);
            }

            currentProject = p;

            for (Bus b : currentProject.getBusses()) {
                connectBus(b);
            }

            currentProject.addProjectChangeListener(projectListener);
        }
    };
    private ProjectChangeListener projectListener = new ProjectChangeListener() {

        @Override
        public void projectNameChanged(Project p, String name) {
        }

        @Override
        public void projectClosed(Project p) {
            currentProject.removeProjectChangeListener(projectListener);

            synchronized (subscriptions) {
                Set<Bus> busses = subscriptions.keySet();
                for (Subscription s : subscriptions.values()) {
                    s.Terminate();
                }
                subscriptions.clear();
            }
        }

        @Override
        public void projectOpened(Project p) {
        }

        @Override
        public void projectBusAdded(Project p, Bus bus) {
            connectBus(bus);
        }

        @Override
        public void projectBusRemoved(Project p, Bus bus) {
            synchronized (subscriptions) {
                Subscription s = subscriptions.get(bus);

                if (s != null) {
                    s.Terminate();
                }
                subscriptions.remove(bus);
            }
        }
    };

    public boolean isBuffering() {
        return isBuffering;
    }

    private FrameListener receiver = new FrameListener() {

        @Override
        public void newFrame(Frame frame) {
            synchronized(frames) {
                frames.add(frame);
            }
        }
    };
    private Runnable cleanupRunnable = new Runnable() {

        private TimeSource ts = TimeSourceManager.getGlobalTimeSource();

        @Override
        public void run() {
            while (!stopRequest) {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    if (stopRequest) {
                        break;
                    } else {
                        logger.log(Level.WARNING, "Snapshot buffer interrupted without stop request");
                    }
                }

                long currentTime = ts.getTime();
                Frame[] frameArray = new Frame[frames.size()];
                synchronized(frames) {
                    frameArray = frames.toArray(frameArray);
                    for (Frame f : frameArray) {
                        if ((f.getTimestamp()/1000) < (currentTime - depth)) {
                            frames.remove(f);
                        } else { /* set is ordered so we can stop here */
                            break;
                        }
                    }
                }
            }

            cleanup();
        }

        private void cleanup() {
            try {
                Thread.sleep(stopTimeout);
            } catch (InterruptedException ex) {
                logger.log(Level.INFO, "Snapshot buffer interrupted while waiting.", ex);
            }
            return;
        }
    };

    public int getDepth() {
        return depth;
    }

    public SnapshotBuffer() {
        ProjectManager.getGlobalProjectManager().addListener(managementListener);
        currentProject = ProjectManager.getGlobalProjectManager().getOpenedProject();
        if (currentProject != null) {
            for (Bus b : currentProject.getBusses()) {
                connectBus(b);
            }

            currentProject.addProjectChangeListener(projectListener);
        }

        logger.log(Level.INFO, "New snapshot buffer was connected");
    }

    private void connectBus(Bus bus) {
        Subscription s = subscriptions.get(bus);

        if (s == null) {
            Subscription sn = new Subscription(receiver, bus);
            subscriptions.put(bus, sn);
            logger.log(Level.INFO, "Connected bus" + bus.getName());
        }
    }

    /**
     * Connect the buffer to a bus to receive frames. This is done through
     * a RAW subscription that is added to the bus.
     * @param bus The bus to receive Frames from
     */
    public void startBuffering() {

        frames.clear();
        for (Subscription s : subscriptions.values()) {
            s.setSubscribeAll(true);
        }

        stopRequest = false;
        cleanupThread = new Thread(cleanupRunnable);
        cleanupThread.setName("Snapshot buffer thread");
        cleanupThread.start();

        isBuffering = true;
    }

    /**
     * Stop buffering from the bus. No more frames will be added. The
     * connection to the bus is terminated.
     */
    public void stopBuffering(int ms) {
        stopTimeout = ms;
        stopRequest = true;

        if (cleanupThread != null && cleanupThread.isAlive()) {
            try {
                cleanupThread.join();
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "was not able to stop cleanup thread!", ex);
            }
        }

        for (Subscription s : subscriptions.values()) {
            s.Terminate();
        }

        isBuffering = false;
    }

    public void writeToFile() {
        String fileName = "Snapshot_" + sdf.format(cal.getTime()) + ".log";

        FileObject logFolder = FileUtil.toFileObject(new File(Options.getLogFilesFolder()));

        OutputStream os = null;
        try {
            FileObject fo = logFolder.createData(fileName);

            os = fo.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter out = new BufferedWriter(osw);
            out.write("PLATFORM SNAPSHOTS\n");
            out.write("DESCRIPTION \"Snapshot of project " + currentProject.getName() + "\"\n");

            Set<Bus> busses = subscriptions.keySet();

            for (Bus bus : busses) {
                if(bus.getAlias() != null && !bus.getAlias().equals(""))
                    out.write("DEVICE_ALIAS " + bus.getAlias() + " " + bus.getName() + "\n");
                else
                    out.write("DEVICE_ALIAS " + bus.getName() + " " + bus.getName() + "\n");
            }

            for (Frame frame : frames) {
                out.write(frame.toLogFileNotation());
            }
            out.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if(os != null)
                    os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
