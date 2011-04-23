/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.logging;

/**
 *
 * @author dschanoeh
 */
public interface LogFileManagementChangeListener {
    
    public void logFilesForPlatformChanged(String platform);
    
    public void platformsChanged();
    
    public void favouritesChanged();
    
}
