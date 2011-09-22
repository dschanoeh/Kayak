/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.gaugeview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameListener;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.Signal;
import com.github.kayak.core.description.SignalDescription;
import com.github.kayak.ui.messageview.MessageSignalDropAdapter;
import com.github.kayak.ui.projects.Project;
import com.github.kayak.ui.projects.ProjectChangeListener;
import com.github.kayak.ui.projects.ProjectManager;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;


@ConvertAsProperties(dtd = "-//com.github.kayak.ui.gaugeview//Gauge//EN",
autostore = false)
@TopComponent.Description(preferredID = "GaugeTopComponent",
iconBase="org/freedesktop/tango/16x16/actions/appointment-new.png",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public final class GaugeTopComponent extends TopComponent {

    private SignalDescription signalDescription;
    private Project project;
    private Subscription subscription;
    private Bus bus;
    private double minimum = 0.0;
    private double maximum = 5.0;
    private double value;
    private static final int RADIUS = 75;
    private static final int BORDER = 25;
    private static final int CENTERX = RADIUS + BORDER;
    private static final int CENTERY = RADIUS;
    private boolean manualRange;

    private MessageSignalDropAdapter.Receiver dropReceiver = new MessageSignalDropAdapter.Receiver() {

        @Override
        public void dropped(SignalDescription signal, Bus b) {
            if(subscription != null) {
                subscription.Terminate();
            }

            int id = signal.getMessageDescription().getId();
            subscription = new Subscription(listener, b);
            subscription.subscribe(id);
            signalDescription = signal;
            jTextField1.setText(signalDescription.getName());
            jTextField5.setText(signalDescription.getUnit());
            setName(NbBundle.getMessage(GaugeTopComponent.class, "CTL_GaugeTopComponent") + " - " + signal.getName());
            bus = b;

            if(project != null)
                project.removeProjectChangeListener(projectChangeListener);

            project = ProjectManager.getGlobalProjectManager().getOpenedProject();
            project.addProjectChangeListener(projectChangeListener);
        }

        @Override
        public void dropped(MessageDescription message, Bus bus) {

        }
    };

    FrameListener listener = new FrameListener() {

        @Override
        public void newFrame(Frame frame) {
            Signal s = signalDescription.decodeData(frame.getData());
            if(s != null)
                updateValue(s.getValue());

        }
    };

    private ProjectChangeListener projectChangeListener = new ProjectChangeListener() {

        @Override
        public void projectNameChanged(Project p, String name) {

        }

        @Override
        public void projectClosed(Project p) {
            close();
        }

        @Override
        public void projectOpened(Project p) {

        }

        @Override
        public void projectBusAdded(Project p, Bus bus) {

        }

        @Override
        public void projectBusRemoved(Project p, Bus b) {
            if(bus == b)
                close();
        }
    };

    private void updateValue(double value) {
        jTextField4.setText(String.format("%.6f", value));

        if(!manualRange) {
            if(minimum > value) {
                minimum = value;
                jTextField2.setText(String.valueOf(minimum));
            }

            if(maximum < value) {
                maximum = value;
                jTextField3.setText(String.valueOf(maximum));
            }
        }

        /* only repaint if there is at least 0.5% change in the value */
        double change = Math.abs(this.value - value)/(maximum - minimum);
        assert change > 0f;


        if(change > 0.005f) {
            this.value = value;
            repaint();
        }
    }

    protected Point ratioToPoint(double ratio) {
        Point p = new Point();
        double radianRatio = ratio * 1.5 * Math.PI;
        double realAngle = (1.25 * Math.PI) - radianRatio;
        p.setLocation(CENTERX + (int) (RADIUS * Math.cos(realAngle)), CENTERY + (int) (RADIUS * Math.sin(-1f * realAngle)));
        return p;
    }

    protected static double scaleElementsForRange(double range) {
        double scaleElement = range / 10f;

        double checkValue = 1f;
        while(true) {
            if(checkValue > scaleElement) { /* go down */
                if(!((range / (checkValue/10f)) > 15f)) { /* not more than 15 elements */
                    checkValue/=10f;
                    continue;
                } else {
                    break;
                }
            } else { /* go up */
                if(!((range / (checkValue*10f) < 5))) { /* not less than 5 elements */
                    checkValue*=10f;
                    continue;
                } else {
                    break;
                }
            }
        }

        return checkValue;
    }

    @Override
    public void paint(Graphics gr) {
        super.paint(gr);
        Graphics2D g = (Graphics2D) gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        /* Scale */
        g.drawArc(BORDER, 0, RADIUS*2, RADIUS*2, -45, 270);
        double range = maximum-minimum;
        double exactScaleElement = scaleElementsForRange(range);

        for(double i=minimum + exactScaleElement;i < maximum;i+=exactScaleElement) {
            Point p = ratioToPoint(i / (maximum-minimum));

            int directionx = (p.x-CENTERX)/16;
            int directiony = (p.y-CENTERY)/16;
            g.drawLine(p.x-directionx, p.y-directiony,
                    p.x+directionx, p.y+directiony);
        }

        /* Center point */
        g.fillOval(CENTERX-5, CENTERY-5, 10, 10);
        double ratio = value/range;

        /* Pointer */
        Point p = ratioToPoint(ratio);
        g.drawLine(CENTERX, CENTERY, p.x, p.y);
    }

    public GaugeTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(GaugeTopComponent.class, "CTL_GaugeTopComponent"));
        setToolTipText(NbBundle.getMessage(GaugeTopComponent.class, "HINT_GaugeTopComponent"));

        DropTarget dt = new DropTarget(jTextField1, new MessageSignalDropAdapter(dropReceiver));
	jTextField1.setDropTarget(dt);

        jTextField2.setText(String.valueOf(minimum));
        jTextField3.setText(String.valueOf(maximum));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(200, 0));
        setPreferredSize(new java.awt.Dimension(200, 265));

        jTextField1.setEditable(false);
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jLabel3.text")); // NOI18N

        jTextField2.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField2.text")); // NOI18N
        jTextField2.setMaximumSize(new java.awt.Dimension(100, 2147483647));
        jTextField2.setMinimumSize(new java.awt.Dimension(100, 20));
        jTextField2.setPreferredSize(new java.awt.Dimension(100, 20));
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField2FocusLost(evt);
            }
        });

        jTextField3.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField3.text")); // NOI18N
        jTextField3.setMaximumSize(new java.awt.Dimension(50, 2147483647));
        jTextField3.setMinimumSize(new java.awt.Dimension(50, 20));
        jTextField3.setPreferredSize(new java.awt.Dimension(50, 20));
        jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField3FocusLost(evt);
            }
        });

        jTextField4.setEditable(false);
        jTextField4.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jLabel4.text")); // NOI18N

        jTextField5.setEditable(false);
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField5.text")); // NOI18N
        jTextField5.setBorder(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel2)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel3))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(157, 157, 157))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusLost
        String text = jTextField2.getText();

        if(!text.isEmpty()) {
            double min = Double.parseDouble(text);
            minimum = min;
            manualRange = true;
            repaint();
        }
    }//GEN-LAST:event_jTextField2FocusLost

    private void jTextField3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField3FocusLost
        String text = jTextField3.getText();

        if(!text.isEmpty()) {
            double max = Double.parseDouble(text);
            maximum = max;
            manualRange = true;
            repaint();
        }
    }//GEN-LAST:event_jTextField3FocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {

    }

    @Override
    public void componentClosed() {
        if(subscription != null) {
            subscription.Terminate();
        }

        if(project != null) {
            project.removeProjectChangeListener(projectChangeListener);
        }
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
