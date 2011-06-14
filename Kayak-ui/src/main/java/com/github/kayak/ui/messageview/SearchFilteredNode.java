/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.BusDescription;
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.SignalDescription;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author dschanoeh
 */
public class SearchFilteredNode extends FilterNode {

    static class SearchFilteredChildren extends FilterNode.Children {

        private final String filterString;

        public SearchFilteredChildren(Node owner, String filterString) {
            super(owner);

            this.filterString = filterString;
        }

        @Override
        protected Node copyNode(Node original) {
            return new SearchFilteredNode(original, filterString);
        }

        private boolean signalMatches(SignalDescription signal, String filter) {
            if(signal.getName().toLowerCase().contains(filter.toLowerCase()))
                return true;

            if(signal.getNotes().toLowerCase().contains(filter.toLowerCase()))
                return true;

            if(signal.getUnit().toLowerCase().contains(filter.toLowerCase()))
                return true;

            return false;
        }

        private boolean messageMatches(MessageDescription message, String filter) {
            if(message.getName().toLowerCase().contains(filter.toLowerCase()))
                return true;

            if(Integer.toHexString(message.getId()).equals(filter))
                return true;

            return false;
        }

        @Override
        protected Node[] createNodes(Node object) {
            List<Node> result = new ArrayList<Node>();

            for (Node node : super.createNodes(object)) {

                /* try to find message */
                MessageDescription messageDescription = (MessageDescription) node.getLookup().lookup(MessageDescription.class);
                if(messageDescription != null) {

                    if(messageMatches(messageDescription, filterString)) {
                        result.add(node);
                    } else {
                        Set<SignalDescription> signals = messageDescription.getSignals();

                        for(SignalDescription signal : signals) {
                            if(signalMatches(signal, filterString)) {
                                result.add(node);
                                break;
                            }
                        }
                    }
                }

                /* try to find signal */
                SignalDescription signalDescription = (SignalDescription) node.getLookup().lookup(SignalDescription.class);
                if(signalDescription != null) {

                    if(signalMatches(signalDescription, filterString)) {
                        result.add(node);
                    }
                }

                /* All bus descriptions will stay in the tree by default */
                BusDescription busDescription = (BusDescription) node.getLookup().lookup(BusDescription.class);
                if(busDescription != null) {
                    result.add(node);
                }

            }

            return result.toArray(new Node[result.size()]);
        }
    }

    public SearchFilteredNode(Node original, String searchFilter) {
        super(original, new SearchFilteredChildren(original, searchFilter));
    }
}
