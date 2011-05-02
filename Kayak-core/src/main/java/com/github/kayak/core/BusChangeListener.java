/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.core;

/**
 *
 * @author dsi9mjn
 */
public interface BusChangeListener {
    public void connectionChanged();
    public void nameChanged();
    public void destroyed();
}
