/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.time;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(category = "Time",
id = "com.github.kayak.ui.time.PauseAction")
@ActionRegistration(iconBase = "org/tango-project/tango-icon-theme/16x16/actions/media-playback-pause.png",
displayName = "com.github.kayak.ui.time.Bundle#CTL_PauseAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/Time", position = 200),
    @ActionReference(path = "Menu/Time", position = 200)
})
public final class PauseAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        TimeSourceManager.getGlobalTimeSource().pause();
    }
}
