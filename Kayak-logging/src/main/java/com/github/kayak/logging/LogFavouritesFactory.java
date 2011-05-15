/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging;

import com.github.kayak.core.LogFile;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author dschanoeh
 */
public class LogFavouritesFactory extends ChildFactory<LogFile> {
    
    private LogFileManagementChangeListener listener = new LogFileManagementChangeListener() {

        @Override
        public void logFilesForPlatformChanged(String platform) {
            
        }

        @Override
        public void platformsChanged() {
            
        }

        @Override
        public void favouritesChanged() {
            refresh(true);
        }
    };
    
    public LogFavouritesFactory() {
        LogFileManager.getGlobalLogFileManager().addListener(listener);
    }

    @Override
    protected boolean createKeys(List<LogFile> toPopulate) {
        List<LogFile> files = LogFileManager.getGlobalLogFileManager().getFavouries();
        toPopulate.addAll(files);
        return true;
    }

    @Override
    protected Node[] createNodesForKey(LogFile key) {
        LogFileNode node = new LogFileNode(key);

        return new Node[] {node};
    }
    
}
