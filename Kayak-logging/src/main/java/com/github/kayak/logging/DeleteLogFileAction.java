/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging;

import com.github.kayak.core.LogFile;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
@ActionRegistration(displayName="Delete Log file...", iconBase="org/tango-project/tango-icon-theme/16x16/actions/edit-delete.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="Log files", id="com.github.kayak.logging.DeleteLogFileAction")
@ActionReferences(value = {
    @ActionReference(path="Menu/Log files", position=40)})
public class DeleteLogFileAction extends AbstractAction {

    LogFile lf;

    public DeleteLogFileAction(LogFile context) {
        putValue(NAME, "Delete...");
        lf = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (lf != null) {
            int res = JOptionPane.showConfirmDialog(null, "", "Are you sure?", JOptionPane.YES_NO_OPTION);

            if (res == JOptionPane.YES_OPTION) {
                File f = lf.getFile();
                f.delete();
                LogFileManager.getGlobalLogFileManager().removeLogFile(lf);
            }
        }

    }
};
