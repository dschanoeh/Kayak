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
package com.github.kayak.mapview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameReceiver;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.description.Signal;
import com.github.kayak.core.description.SignalDescription;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.dnd.DropTarget;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

@ConvertAsProperties(dtd = "-//com.github.kayak.mapview//MapView//EN",
autostore = false)
@TopComponent.Description(preferredID = "MapViewTopComponent",
iconBase="org/freedesktop/tango/16x16/apps/internet-web-browser.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.github.kayak.mapview.MapViewTopComponent")
@ActionReferences( value = {
   @ActionReference(path = "Menu/Bus views" /*, position = 333 */),
   @ActionReference(path = "Toolbars/Bus views" /*, position = 333 */)
})
@TopComponent.OpenActionRegistration(displayName = "#CTL_MapViewAction",
preferredID = "MapViewTopComponent")
public final class MapViewTopComponent extends TopComponent {

    private JXMapKit mapKit = new JXMapKit();
    Set<Waypoint> waypoints;
    double latitude, longitude;

    private SignalDescriptionDropTargetAdapter.SignalDescriptionDropReceiver latitudeDropReceiver = new SignalDescriptionDropTargetAdapter.SignalDescriptionDropReceiver() {

        Bus b;
        Subscription s;
        SignalDescription description;

        @Override
        public void receive(Bus b, SignalDescription desc) {

            if(s != null) {
                s.Terminate();
            }

            jTextField3.setText(desc.getName());
            description = desc;

            FrameReceiver latitudeReceiver = new FrameReceiver() {

                @Override
                public void newFrame(Frame frame) {
                    if(description != null && frame.getIdentifier() == description.getMessage().getId()) {
                        Signal s = description.decodeData(frame.getData());
                        latitude = Double.parseDouble(s.getValue());

                        addWaypoint(latitude, longitude);
                    }
                }
            };
            
            int id = desc.getMessage().getId();
            Subscription s = new Subscription(latitudeReceiver, b);
            s.subscribe(id);
        }
    };

    private SignalDescriptionDropTargetAdapter.SignalDescriptionDropReceiver longitudeDropReceiver = new SignalDescriptionDropTargetAdapter.SignalDescriptionDropReceiver() {

        Bus b;
        Subscription s;
        SignalDescription description;

        @Override
        public void receive(Bus b, SignalDescription desc) {

            if(s != null) {
                s.Terminate();
            }

            jTextField4.setText(desc.getName());
            description = desc;

            FrameReceiver longitudeReceiver = new FrameReceiver() {

                @Override
                public void newFrame(Frame frame) {
                    if(description != null && frame.getIdentifier() == description.getMessage().getId()) {
                        Signal s = description.decodeData(frame.getData());
                        longitude = Double.parseDouble(s.getValue());

                        addWaypoint(latitude, longitude);
                    }
                }
            };
            
            int id = desc.getMessage().getId();
            Subscription s = new Subscription(longitudeReceiver, b);
            s.subscribe(id);
        }
    };

    public MapViewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(MapViewTopComponent.class, "CTL_MapViewTopComponent"));
        setToolTipText(NbBundle.getMessage(MapViewTopComponent.class, "HINT_MapViewTopComponent"));
        
        mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
        
        GeoPosition p = new GeoPosition(52.42182, 10.78498);
        mapKit.setAddressLocation(p);
        mapKit.setMiniMapVisible(false);
        mapKit.setZoomSliderVisible(false);
        mapKit.setZoom(0);
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(mapKit, BorderLayout.CENTER);
        
        waypoints = Collections.synchronizedSet(new HashSet<Waypoint>());
        WaypointPainter painter = new WaypointPainter();
        painter.setRenderer(new WaypointRenderer() {  
      
            @Override
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {  
                g.setColor(Color.RED);  
                g.fillRoundRect(-10, -10, 10, 10, 10, 10);  
                return true;  
            }  
        });
        painter.setWaypoints(waypoints);
        mapKit.getMainMap().setOverlayPainter(painter);

        DropTarget latitudeDropTarget = new DropTarget(jTextField1, new SignalDescriptionDropTargetAdapter(latitudeDropReceiver));
        jTextField3.setDropTarget(latitudeDropTarget);
        
        DropTarget longitudeDropTarget = new DropTarget(jTextField1, new SignalDescriptionDropTargetAdapter(longitudeDropReceiver));
        jTextField4.setDropTarget(longitudeDropTarget);
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPanel2 = new javax.swing.JPanel();
                jPanel1 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                jLabel2 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jTextField2 = new javax.swing.JTextField();
                jPanel3 = new javax.swing.JPanel();
                jLabel3 = new javax.swing.JLabel();
                jButton1 = new javax.swing.JButton();
                jLabel4 = new javax.swing.JLabel();
                jLabel5 = new javax.swing.JLabel();
                jTextField3 = new javax.swing.JTextField();
                jTextField4 = new javax.swing.JTextField();
                jButton2 = new javax.swing.JButton();

                javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 599, Short.MAX_VALUE)
                );
                jPanel2Layout.setVerticalGroup(
                        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 233, Short.MAX_VALUE)
                );

                org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jLabel1.text")); // NOI18N

                org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jLabel2.text")); // NOI18N

                jTextField1.setEditable(false);
                jTextField1.setText(org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jTextField1.text")); // NOI18N

                jTextField2.setEditable(false);
                jTextField2.setText(org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jTextField2.text")); // NOI18N

                jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jPanel3.border.title"))); // NOI18N

                org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jLabel3.text")); // NOI18N

                org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jButton1.text")); // NOI18N
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jLabel4.text")); // NOI18N

                org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jLabel5.text")); // NOI18N

                jTextField3.setText(org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jTextField3.text")); // NOI18N

                jTextField4.setText(org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jTextField4.text")); // NOI18N

                org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jButton2.text")); // NOI18N
                jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                jButton2MouseClicked(evt);
                        }
                });

                javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
                jPanel3.setLayout(jPanel3Layout);
                jPanel3Layout.setHorizontalGroup(
                        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel4)
                                                .addGap(3, 3, 3)
                                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(32, 32, 32)
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton2))
                                        .addComponent(jButton1))
                                .addContainerGap(111, Short.MAX_VALUE))
                );
                jPanel3Layout.setVerticalGroup(
                        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jLabel5)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                );

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                                .addGap(212, 212, 212))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap())))
                );
                jPanel1Layout.setVerticalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                );

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
            waypoints.clear();
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
            
        }//GEN-LAST:event_jButton2MouseClicked

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        private javax.swing.JTextField jTextField3;
        private javax.swing.JTextField jTextField4;
        // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    private void addWaypoint(double latitude, double longitude) {
        mapKit.setAddressLocation(new GeoPosition(latitude, longitude));
        waypoints.add(new Waypoint(latitude, longitude));
        jTextField1.setText(Double.toString(latitude));
        jTextField2.setText(Double.toString(longitude));
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
