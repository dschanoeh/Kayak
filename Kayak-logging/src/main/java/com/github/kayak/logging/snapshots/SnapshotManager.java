/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging.snapshots;

import com.github.kayak.core.TimeEventReceiver;
import com.github.kayak.ui.time.TimeSourceManager;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class SnapshotManager {

    private static SnapshotBuffer buffer;

    /**
     * Returns the current active {@link SnapshotBuffer}. If no SnapshotBuffer
     * is present one is created and buffering is started.
     * This method ensures that a new buffer is created after invocation.
     * @return
     */
    public static SnapshotBuffer getCurrentBuffer() {
        SnapshotBuffer oldbuffer = buffer;

        if(oldbuffer == null) {
            oldbuffer = new SnapshotBuffer();
            oldbuffer.startBuffering();
        }

        buffer = new SnapshotBuffer();
        startBuffering();

        return oldbuffer;
    }

    /**
     * Create a new SnapshotBuffer and start buffering.
     */
    public static void startBuffering() {
        if(buffer == null)
            buffer = new SnapshotBuffer();

        if(!buffer.isBuffering())
            buffer.startBuffering();
    }

    public static void enableBuffering() {
        TimeSourceManager.getGlobalTimeSource().register(receiver);
    }

    private static TimeEventReceiver receiver = new TimeEventReceiver() {

        public void paused() {

        }
	public void played() {
            buffer = new SnapshotBuffer();
            buffer.startBuffering();

        }
	public void stopped() {
            if(buffer != null && buffer.isBuffering()) {
                buffer.stopBuffering(0);
            }
        }
    };

}
