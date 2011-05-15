/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.logging.output;

import com.github.kayak.core.Bus;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author dschanoeh
 */
public class BusNodeFactory extends ChildFactory<Bus> {

    List<Bus> busses;
    private NotifyDelete notify;

    public BusNodeFactory(List<Bus> busses, NotifyDelete notify) {
        this.busses = busses;
        this.notify = notify;
    }

    @Override
    protected boolean createKeys(List<Bus> list) {
        list.addAll(busses);

        return true;
    }

    @Override
    protected Node[] createNodesForKey(Bus key) {
        BusNode node = new BusNode(key, notify);
        return new Node[] {node};
    }

    public void update() {
        refresh(true);
    }


}
