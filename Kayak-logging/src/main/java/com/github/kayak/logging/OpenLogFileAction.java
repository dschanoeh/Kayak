/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging;

import com.github.kayak.core.LogFile;
import com.github.kayak.logging.input.LogInputTopComponent;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
@ActionRegistration(displayName = "Open Log file...", iconBase = "org/tango-project/tango-icon-theme/16x16/actions/document-open.png", iconInMenu = true, surviveFocusChange = true)
@ActionID(category = "Log files", id = "com.github.kayak.logging.OpenLogFileAction")
@ActionReferences(value = {
    @ActionReference(path = "Menu/Log files", position = 10)})
public class OpenLogFileAction extends AbstractAction {

    private LogFile logFile;

    public OpenLogFileAction(LogFile context) {
        putValue(NAME, "Open");
        logFile = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LogInputTopComponent tc = new LogInputTopComponent();
        tc.setLogFile(logFile);
        tc.open();
        tc.requestActive();
    }
};
