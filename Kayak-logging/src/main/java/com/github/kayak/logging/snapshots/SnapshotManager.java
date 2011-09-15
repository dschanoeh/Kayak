/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging.snapshots;

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

}
