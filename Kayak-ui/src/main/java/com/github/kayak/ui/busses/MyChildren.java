/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.busses;

import com.github.kayak.core.Bus;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author dsi9mjn
 */
public class MyChildren extends Children.Keys {

    @Override
    protected Node[] createNodes(Object key) {
        Bus bus = (Bus) key;
        return new BusNode[] { new BusNode(bus) };
    }


}
