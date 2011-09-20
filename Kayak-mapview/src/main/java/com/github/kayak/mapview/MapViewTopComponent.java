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
import com.github.kayak.core.FrameListener;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.description.Signal;
import com.github.kayak.core.description.SignalDescription;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
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

    private static final Logger logger = Logger.getLogger(MapViewTopComponent.class.getName());

    private JXMapKit mapKit = new JXMapKit();
    private final List<Waypoint> waypoints = Collections.synchronizedList(new ArrayList<Waypoint>());
    double latitude, longitude;
    long timeLastLat = -1; /* Time of the last latitude update */
    long timeLastLong = -1; /* Time of the last longitude update */
    long timeLatLong = -1; /* Time between latitude and longitude updates */
    long timeLongLat = -1; /* Time between longitude and latitude updates */

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

            FrameListener latitudeReceiver = new FrameListener() {

                @Override
                public void newFrame(Frame frame) {
                    if(description != null && frame.getIdentifier() == description.getMessageDescription().getId()) {
                        Signal s = description.decodeData(frame.getData());
                        latitude = s.getValue();
                        long timestamp = frame.getTimestamp();
                        if(timeLastLat != -1 && timeLastLong != -1) {
                            timeLongLat = frame.getTimestamp() - timeLastLong;
                            if(timeLatLong > timeLongLat) {
                                addWaypoint(latitude, longitude);
                            }
                        }
                        timeLastLat = timestamp;
                    }
                }
            };

            int id = desc.getMessageDescription().getId();
            s = new Subscription(latitudeReceiver, b);
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

            FrameListener longitudeReceiver = new FrameListener() {

                @Override
                public void newFrame(Frame frame) {
                    if(description != null && frame.getIdentifier() == description.getMessageDescription().getId()) {
                        Signal s = description.decodeData(frame.getData());
                        longitude = s.getValue();
                        long timestamp = frame.getTimestamp();
                        if(timeLastLat != -1 && timeLastLong != -1) {
                            timeLatLong = frame.getTimestamp() - timeLastLat;
                            if(timeLatLong < timeLongLat) {
                                addWaypoint(latitude, longitude);
                            }
                        }
                        timeLastLong = timestamp;
                    }
                }
            };

            int id = desc.getMessageDescription().getId();
            s = new Subscription(longitudeReceiver, b);
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

        Painter<JXMapViewer> textOverlay = new Painter<JXMapViewer>() {

            @Override
            public void paint(Graphics2D g, JXMapViewer t, int i, int i1) {
                /* Draw latitude longitude information */
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                String s = String.format("Latitude: %f Longitude: %f", latitude, longitude);
                g.setPaint(new Color(0,0,0,150));
                g.fillRoundRect(10, 10, 220 , 30, 10, 10);
                g.setPaint(Color.WHITE);
                g.drawString(s, 10+10, 10+20);

                /* Draw waypoints */
                g.setPaint(Color.RED);
                Rectangle rect =  t.getViewportBounds();
                g.translate(-rect.x, -rect.y);

                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2));

                int lastX = -1;
                int lastY = -1;
                synchronized(waypoints) {
                    for(Waypoint w : waypoints) {
                        Point2D pt = t.getTileFactory().geoToPixel(w.getPosition(), t.getZoom());

                        if(lastX != -1 && lastY != -1) {
                            g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
                        }

                        lastX = (int) pt.getX();
                        lastY = (int) pt.getY();
                    }
                }
            }
        };

        mapKit.getMainMap().setOverlayPainter(textOverlay);

        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(mapKit, BorderLayout.CENTER);

        DropTarget latitudeDropTarget = new DropTarget(jTextField3, new SignalDescriptionDropTargetAdapter(latitudeDropReceiver));
        jTextField3.setDropTarget(latitudeDropTarget);

        DropTarget longitudeDropTarget = new DropTarget(jTextField4, new SignalDescriptionDropTargetAdapter(longitudeDropReceiver));
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
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 599, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 280, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jPanel3.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jLabel3.text")); // NOI18N

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
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(3, 3, 3)
                        .addComponent(jTextField3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap(256, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jToggleButton1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton1, org.openide.util.NbBundle.getMessage(MapViewTopComponent.class, "MapViewTopComponent.jToggleButton1.text")); // NOI18N
        jToggleButton1.setFocusable(false);
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    private void addWaypoint(double latitude, double longitude) {
        if(jToggleButton1.isSelected())
            mapKit.setAddressLocation(new GeoPosition(latitude, longitude));
        waypoints.add(new Waypoint(latitude, longitude));
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}
