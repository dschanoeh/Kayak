/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.descriptions;

import com.github.kayak.core.description.Document;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author dschanoeh
 */
public class DescriptionsNodeFactory extends ChildFactory<Document> {

    @Override
    protected boolean createKeys(List<Document> list) {
        list.addAll(DescriptionManagement.getGlobalDescriptionManagement().getDescriptions());
        return true;
    }

    @Override
    protected Node[] createNodesForKey(Document key) {
        return new Node[] { new DocumentNode(key) };
    }

}
