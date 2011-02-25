/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.time;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class StopAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        TimeSourceManager.getGlobalTimeSource().stop();
    }
}
