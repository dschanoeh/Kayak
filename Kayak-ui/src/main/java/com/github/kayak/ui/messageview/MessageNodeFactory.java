/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.BusDescription;
import com.github.kayak.core.description.MessageDescription;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author dschanoeh
 */
public class MessageNodeFactory extends ChildFactory<MessageDescription> {

    private BusDescription description;
    
    public MessageNodeFactory(BusDescription description) {
	this.description = description;	
    }
    
    @Override
    protected boolean createKeys(List<MessageDescription> list) {
	for(Integer key : description.getMessages().keySet()) {
	    list.add(description.getMessages().get(key));
	}

	return true;
    }

    @Override
    protected Node[] createNodesForKey(MessageDescription key) {
	return new Node[] { new MessageDescriptionNode(key) };	     
    }
    
}
