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
import com.github.kayak.core.FrameReceiver;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.TimeSource;
import com.github.kayak.ui.projects.Project;
import java.io.IOException;
import java.util.ArrayList;
import com.github.kayak.core.TimeSource.Mode;
import com.github.kayak.ui.projects.ProjectChangeListener;
import com.github.kayak.ui.projects.ProjectManagementListener;
import com.github.kayak.ui.projects.ProjectManager;
import com.github.kayak.ui.time.TimeSourceManager;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
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

    private int depth = 5000;
    private final ArrayList<Frame> frames;
    private Thread cleanupThread;
    private int stopTimeout = 0;
    private boolean stopRequest = false;
    private Mode mode;
    private ArrayList<Bus> busses;
    private ArrayList<Subscription> subscriptions;
    private String name;
    private String platform;
    private String description;
    private Project currentProject;
    private String path;
    private boolean wasWritten = false;
    
    private ProjectManagementListener managementListener = new ProjectManagementListener() {

        @Override
        public void projectsUpdated() {
            
        }

        @Override
        public void openProjectChanged(Project p) {
            logger.log(Level.INFO, "Switching snapshot buffer to new project");
            if(currentProject != null)
                currentProject.removeProjectChangeListener(projectListener);
            
            p.addProjectChangeListener(projectListener);
        }
    };
    
    private ProjectChangeListener projectListener = new ProjectChangeListener() {

        @Override
        public void projectNameChanged() {
            
        }

        @Override
        public void projectClosed() {
            currentProject.removeProjectChangeListener(projectListener);
            
            for(Subscription s : subscriptions) {
                s.Terminate();
            }
            
            busses.clear();
            subscriptions.clear();
        }

        @Override
        public void projectOpened() {

        }

        @Override
        public void projectBusAdded(Bus bus) {
            
        }

        @Override
        public void projectBusRemoved(Bus bus) {
            busses.remove(bus);
        }
    };

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    private FrameReceiver receiver = new FrameReceiver() {

        @Override
        public void newFrame(Frame frame) {
            synchronized (frames) {
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    if (stopRequest) {
                        try {
                            Thread.sleep(stopTimeout);
                        } catch (InterruptedException ex) {
                            logger.log(Level.WARNING, "Snapshot buffer interrupted while waiting.", ex);
                        }

                        for(Subscription s : subscriptions) {
                            s.Terminate();
                        }
                        subscriptions.clear();
                        busses.clear();
                    }
                }
                long currentTime = ts.getTime();
                synchronized (frames) {
                    Frame[] frameArray = new Frame[0];
                    frameArray = frames.toArray(frameArray);
                    for (Frame f : frameArray) {
                        if (f.getTimestamp() < currentTime - depth) {
                            frames.remove(f);
                        }
                    }
                }
            }
        }
    };

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (depth > 0) {
            this.depth = depth;
        }
    }

    public SnapshotBuffer() {
        frames = new ArrayList<Frame>(500);
        busses = new ArrayList<Bus>();
        subscriptions = new ArrayList<Subscription>();
        
        ProjectManager.getGlobalProjectManager().addListener(managementListener);
        currentProject = ProjectManager.getGlobalProjectManager().getOpenedProject();
        if(currentProject != null)
            currentProject.addProjectChangeListener(projectListener);
    }

    public void connectBus(Bus bus) {
        busses.add(bus);
        Subscription s = new Subscription(receiver, bus);
        bus.addSubscription(s);
        subscriptions.add(s);
    }

    /**
     * Connect the buffer to a bus to receive frames. This is done through
     * a RAW subscription that is added to the bus.
     * @param bus The bus to receive Frames from
     */
    public void startBuffering() {

        frames.clear();
        for(Subscription s : subscriptions) {
            s.setSubscribeAll(true);
        }

        stopRequest = false;
        cleanupThread = new Thread(cleanupRunnable);
        cleanupThread.start();
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
        
        for(Subscription s : subscriptions) {
            s.Terminate();
        }
        subscriptions.clear();
        busses.clear();
    }

    public void writeToFile(FileObject fo) {
        path = fo.getPath();
        OutputStream os = null;
        try {
            os = fo.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);            
            BufferedWriter out = new BufferedWriter(osw);
            out.write("PLATFORM " + platform + "\n");
            out.write("DESCRIPTION \"" + description + "\"\n");
            
            HashSet<String> busNames = new HashSet<String>();
            for(Frame frame : frames) {
                busNames.add(frame.getBusName());
            }
            for(String name : busNames) {
                out.write("DEVICE_ALIAS " + name + " " + name + "\n");
            }
            
            for(Frame frame : frames) {
                out.write(frame.toLogFileNotation());
            }
            out.close();
            wasWritten = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        wasWritten = true;
    }

    @Override
    public String toString() {
        if(name != null)
            return name;
        else
            return super.toString();
    }
    
    
}
