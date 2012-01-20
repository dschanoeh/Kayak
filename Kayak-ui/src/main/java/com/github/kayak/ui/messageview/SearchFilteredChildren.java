/**
 * 	This file is part of Kayak.
 *
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.MultiplexDescription;
import com.github.kayak.core.description.SignalDescription;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class SearchFilteredChildren extends FilterNode.Children {

        private static final Logger logger = Logger.getLogger(SearchFilteredChildren.class.getCanonicalName());
        private final String filterString;

        public SearchFilteredChildren(Node original, String filterString) {
            super(original);

            this.filterString = filterString;
        }

        @Override
        protected Node copyNode(Node original) {
            return new SearchFilteredNode(original, filterString);
        }

        private boolean signalMatches(SignalDescription signal, String filter) {
            if(signal.getName().toLowerCase().contains(filter.toLowerCase()))
                return true;

            String notes = signal.getNotes();
            if(notes != null)
                if(signal.getNotes().toLowerCase().contains(filter.toLowerCase()))
                    return true;

            String unit = signal.getUnit();
            if(unit != null)
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
        protected Node[] createNodes(Node node) {

            /* Signals have no children */
            if(node instanceof SignalDescriptionNode) {
                SignalDescription signalDescription = ((SignalDescriptionNode) node).getDescription();
                if(signalMatches(signalDescription, filterString))
                    return new Node[] { new FilterNode(node)};

            } else if(node instanceof MessageDescriptionNode) {
                MessageDescription messageDescription = ((MessageDescriptionNode) node).getDescription();

                if(messageDescription != null) {
                    /* add message directly if name matches */
                    if(messageMatches(messageDescription, filterString)) {
                        return new Node[] { new FilterNode(node)};
                    /* add message if signals match */
                    } else {
                        Set<SignalDescription> signals = messageDescription.getSignals();

                        for(SignalDescription signal : signals) {
                            if(signalMatches(signal, filterString)) {
                                return new Node[] {copyNode(node)};
                            }
                        }
                    }
                }

            } else if(node instanceof BusNode) {
                return new Node[] {copyNode(node)};

            } else if(node instanceof MultiplexDescriptionNode) {
                MultiplexDescription multiplexDescription = ((MultiplexDescriptionNode) node).getDescription();

                if(multiplexDescription != null) {
                    Set<SignalDescription> signals = multiplexDescription.getAllSignalDescriptions();

                    for(SignalDescription signal : signals) {
                        if(signalMatches(signal, filterString)) {
                            return new Node[] {copyNode(node)};
                        }
                    }
                }
            } else {
                Logger.getLogger(SearchFilteredNode.class.getCanonicalName()).log(Level.INFO, node.getClass().getName());
            }

            return new Node[] {};
        }
    }
