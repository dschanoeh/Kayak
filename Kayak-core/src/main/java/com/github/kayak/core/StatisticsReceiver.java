/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.core;

/**
 *
 * @author dschanoeh
 */
public interface StatisticsReceiver {
    
    public void statisticsUpdated(long rxBytes, long rxPackets, long tBytes, long tPackets);

}
