/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging;

import com.github.kayak.core.LogFile;
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
@ActionRegistration(displayName="Bookmark Log file...", iconBase="org/freedesktop/tango/16x16/actions/bookmark-new.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="Log files", id="com.github.kayak.logging.BookmarkLogFileAction")
@ActionReferences(value = {
    @ActionReference(path="Menu/Log files", position=30)}) 
public class BookmarkLogFileAction extends AbstractAction {
    LogFile file;

    public BookmarkLogFileAction(LogFile context) {
            putValue (NAME, "Bookmark");            
            file = context;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (file != null) {
                LogFileManager.getGlobalLogFileManager().addFavourite(file);
            }
        }
    
}
