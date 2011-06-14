/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.SignalDescription;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author dschanoeh
 */
public class SignalNodeFactory extends ChildFactory<SignalDescription> {

    private MessageDescription description;

    public SignalNodeFactory(MessageDescription description) {
        this.description = description;
    }

    
    @Override
    protected boolean createKeys(List<SignalDescription> list) {
        list.addAll(description.getSignals());

        return true;
    }

    @Override
    protected Node[] createNodesForKey(SignalDescription key) {
        return new Node[] { new SignalDescriptionNode(key) };
    }
    
}
