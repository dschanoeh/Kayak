/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.kayak.ui.connections;

import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author dsi9mjn
 */
public class BusBeaconDiscoveryFactory extends ChildFactory<BusURL> {
    private List<BusURL> beacons;
    private Thread discoveryThread;

    private Runnable discoveryRunnable = new Runnable() {

        @Override
        public void run() {
            beacons.add(BusURL.fromString("socket://can0@127.0.0.1:28600"));
            refresh(true);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            beacons.add(BusURL.fromString("socket://can0@129.0.0.1:28600"));
            refresh(true);
        }
    };

    public BusBeaconDiscoveryFactory() {
        beacons = new ArrayList<BusURL>();

        discoveryThread = new Thread(discoveryRunnable);
        discoveryThread.start();
    }

    @Override
    protected Node createNodeForKey(BusURL key) {
        BusURLNode node = new BusURLNode(key, BusURLNode.Type.DISCOVERY);
        return node;
    }

    @Override
    protected boolean createKeys(List<BusURL> toPopulate) {
        toPopulate.addAll(beacons);
        return true;
    }
}
