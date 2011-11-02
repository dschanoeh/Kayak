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
id = "com.github.kayak.ui.time.PlayAction")
@ActionRegistration(iconBase = "org/tango-project/tango-icon-theme/16x16/actions/media-playback-start.png",
displayName = "com.github.kayak.ui.time.Bundle#CTL_PlayAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/Time", position = 100),
    @ActionReference(path = "Menu/Time", position = 100)
})
public final class PlayAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        TimeSourceManager.getGlobalTimeSource().play();
    }
}
