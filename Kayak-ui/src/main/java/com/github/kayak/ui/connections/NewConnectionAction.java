/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.connections;

import com.github.kayak.core.BusURL;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(id = "com.github.kayak.ui.connections.NewConnectionAction", category = "Connections")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_NewConnectionAction", iconBase = "org/freedesktop/tango/16x16/actions/document-new.png")
@ActionReferences(value = {
    @ActionReference(path = "Menu/Connections", position = 20)})
public final class NewConnectionAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ev) {
        String url = JOptionPane.showInputDialog("Please type in a socket to connect with.", "socket://can0@192.168.30.129:28640");

        if (url != null) {
            BusURL beacon = BusURL.fromString(url);
            if (beacon != null) {
                ConnectionManager.getGlobalConnectionManager().addRecent(beacon);
            }
        }
    }
}
