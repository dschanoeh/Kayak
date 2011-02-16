/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.busses;

import com.github.kayak.core.Bus;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dsi9mjn
 */
public class BusRootNode extends AbstractNode {
    public BusRootNode() {
        super(Children.create(new BusChildFactory(), false));

    }

    /*@Override
    public Action[] getActions(boolean popup) {
        DataFolder rssFeedsFolder = DataFolder.findFolder(FileUtil.getConfigFile("RssFeeds"));
        return new Action[]{new AddFeedAction(rssFeedsFolder)};
    }*/

    private static class BusChildFactory extends ChildFactory<Bus> implements LookupListener {

        private Result<Bus> result;

        BusChildFactory() {
            result = Lookups.forPath("RssFeeds").lookupResult(Bus.class);
            result.addLookupListener(this);
        }

        @Override
        public void resultChanged(LookupEvent le) {
            refresh(true);
        }

        @Override
        protected boolean createKeys(List<Bus> list) {
            list.addAll(result.allInstances());
            return true;
        }

        @Override
        protected Node createNodeForKey(Bus key) {
            BusNode bn = null;
            
            bn = new BusNode(key);
             
            return bn;
        }

    }


}
