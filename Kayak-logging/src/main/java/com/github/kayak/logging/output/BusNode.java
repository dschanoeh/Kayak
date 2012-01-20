/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.logging.output;

import com.github.kayak.core.Bus;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author dschanoeh
 */
public class BusNode extends AbstractNode {

    private Bus bus;
    private NotifyDelete notify;

    public BusNode(Bus bus, NotifyDelete notify) {
        super(Children.LEAF);

        this.bus = bus;
        this.notify = notify;

        setName(bus.toString());
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { new RemoveAction() };
    }

    private class RemoveAction extends AbstractAction {

        public RemoveAction() {
            putValue(NAME, "Remove");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
           notify.delete(bus);
        }

    };


}
