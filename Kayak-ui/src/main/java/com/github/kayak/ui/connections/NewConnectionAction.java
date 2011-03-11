/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.connections;

import com.github.kayak.core.BusURL;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

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
