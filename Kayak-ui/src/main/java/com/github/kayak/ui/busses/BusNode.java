/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.busses;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import com.github.kayak.core.Bus;

/**
 *
 * @author dsi9mjn
 */
public class BusNode extends AbstractNode {

    public BusNode(Bus bus) {
        super(Children.LEAF);

        setDisplayName(bus.toString());
    }

    public BusNode() {
        super(new MyChildren());
        setDisplayName("Root");
    }

}