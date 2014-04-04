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
package com.github.kayak.ui.gaugeview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameListener;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.description.DescriptionException;
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.MultiplexDescription;
import com.github.kayak.core.description.Signal;
import com.github.kayak.core.description.SignalDescription;
import com.github.kayak.ui.messageview.MessageSignalDropAdapter;
import com.github.kayak.ui.projects.Project;
import com.github.kayak.ui.projects.ProjectChangeListener;
import com.github.kayak.ui.projects.ProjectManager;
import com.github.kayak.ui.useroutput.UserOutput;
import java.awt.dnd.DropTarget;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import eu.hansolo.steelseries.gauges.Radial;
import java.awt.BorderLayout;
import java.awt.Dimension;


@ConvertAsProperties(dtd = "-//com.github.kayak.ui.gaugeview//Gauge//EN",
autostore = false)
@TopComponent.Description(preferredID = "GaugeTopComponent",
iconBase="org/tango-project/tango-icon-theme/16x16/actions/appointment-new.png",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public final class GaugeTopComponent extends TopComponent {

    private SignalDescription signalDescription;
    private Project project;
    private Subscription subscription;
    private Bus bus;
    private double minimum = 0.0;
    private double maximum = 100.0;
    private double value;
    private boolean manualRange;
    private Radial gauge;
    private boolean settings=true;

    private MessageSignalDropAdapter.Receiver dropReceiver = new MessageSignalDropAdapter.Receiver() {

        @Override
        public void dropped(SignalDescription signal, Bus b) {
            if(subscription != null) {
                subscription.Terminate();
            }

            int id = signal.getMessageDescription().getId();
            subscription = new Subscription(listener, b);
            subscription.subscribe(id, signal.getMessageDescription().isExtended());
            signalDescription = signal;
            gauge.setUnitString(signalDescription.getUnit());
            gauge.setTitle(signalDescription.getName());
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
        
        @Override
        public void dropped(MultiplexDescription multiplex, Bus b) {
            if(subscription != null) {
                subscription.Terminate();
            }
            
            SignalDescription signal = multiplex.getMultiplexAsSignal();

            int id = signal.getMessageDescription().getId();
            subscription = new Subscription(listener, b);
            subscription.subscribe(id, signal.getMessageDescription().isExtended());
            signalDescription = signal;
            gauge.setUnitString(signalDescription.getUnit());
            gauge.setTitle(signalDescription.getName());
            setName(NbBundle.getMessage(GaugeTopComponent.class, "CTL_GaugeTopComponent") + " - " + signal.getName());
            bus = b;

            if(project != null)
                project.removeProjectChangeListener(projectChangeListener);

            project = ProjectManager.getGlobalProjectManager().getOpenedProject();
            project.addProjectChangeListener(projectChangeListener);
        }
    };

    FrameListener listener = new FrameListener() {

        @Override
        public void newFrame(Frame frame) {
            try {
                if(frame.isExtended() == signalDescription.getMessageDescription().isExtended()) {
                    Signal s = signalDescription.decodeData(frame.getData());

                    if(s != null)
                    updateValue(s.getValue());
                }
            } catch (DescriptionException ex) {
                UserOutput.printWarning(ex.getMessage());
            }


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

        if(!manualRange) {
            if(minimum > value) {
                minimum = 1.1 * value;
                gauge.setMinValue(minimum);
            }

            if(maximum < value) {
                maximum = 1.1 * value;
                gauge.setMaxValue(maximum);
            }
        }

        /* only repaint if there is at least 0.5% change in the value */
        double change = Math.abs(this.value - value)/(maximum - minimum);


        if(change > 0.005f) {
            this.value = value;
            gauge.setValue(value);
        }
    }

    public GaugeTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(GaugeTopComponent.class, "CTL_GaugeTopComponent"));
        setToolTipText(NbBundle.getMessage(GaugeTopComponent.class, "HINT_GaugeTopComponent"));

        DropTarget dt = new DropTarget(jPanel1, new MessageSignalDropAdapter(dropReceiver));
	jPanel1.setDropTarget(dt);

        jTextField2.setText(String.valueOf(minimum));
        jTextField3.setText(String.valueOf(maximum));

        gauge = new Radial();
        gauge.setTitle("Drag Signal");
        gauge.setUnitString("here");
        gauge.setMinimumSize(new Dimension(100, 100));
        gauge.setGlowVisible(true);
        gauge.setMaxValue(maximum);
        gauge.setMinValue(minimum);
        jPanel1.add(gauge, BorderLayout.CENTER);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jToggleButton4 = new javax.swing.JToggleButton();

        setMinimumSize(new java.awt.Dimension(200, 0));
        setPreferredSize(new java.awt.Dimension(200, 265));
        setLayout(new java.awt.BorderLayout());

        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.setLayout(new java.awt.BorderLayout());
        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jPanel2.border.title"))); // NOI18N
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jTextField3.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField3.text")); // NOI18N
        jTextField3.setMaximumSize(new java.awt.Dimension(50, 2147483647));
        jTextField3.setMinimumSize(new java.awt.Dimension(100, 20));
        jTextField3.setPreferredSize(new java.awt.Dimension(100, 20));
        jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField3FocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextField3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        jPanel2.add(jLabel2, gridBagConstraints);

        jTextField2.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField2.text")); // NOI18N
        jTextField2.setMaximumSize(new java.awt.Dimension(100, 2147483647));
        jTextField2.setMinimumSize(new java.awt.Dimension(100, 20));
        jTextField2.setPreferredSize(new java.awt.Dimension(100, 20));
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField2FocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jTextField2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        jPanel2.add(jLabel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton2, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jToggleButton2.text")); // NOI18N
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jToggleButton2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton3, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jToggleButton3.text")); // NOI18N
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jToggleButton3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jLabel1, gridBagConstraints);

        jTextField1.setText(org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jTextField1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jTextField1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton4, org.openide.util.NbBundle.getMessage(GaugeTopComponent.class, "GaugeTopComponent.jToggleButton4.text")); // NOI18N
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        jPanel2.add(jToggleButton4, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusLost
        String text = jTextField2.getText();

        if(!text.isEmpty()) {
            double min = Double.parseDouble(text);
            minimum = min;
            manualRange = true;
            gauge.setMinValue(min);
        }
    }//GEN-LAST:event_jTextField2FocusLost

    private void jTextField3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField3FocusLost
        String text = jTextField3.getText();

        if(!text.isEmpty()) {
            double max = Double.parseDouble(text);
            maximum = max;
            manualRange = true;
            gauge.setMaxValue(max);
        }
    }//GEN-LAST:event_jTextField3FocusLost

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
        if(settings) {
            remove(jPanel2);
            settings = false;
            gauge.repaint();
            gauge.revalidate();
            repaint();
        } else {
            add(jPanel2, BorderLayout.NORTH);
            settings = true;
            gauge.repaint();
            gauge.revalidate();
            repaint();
        }
    }//GEN-LAST:event_jPanel1MouseClicked

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        if(jToggleButton2.isSelected()) {
            gauge.setMaxMeasuredValueVisible(true);
            gauge.setMinMeasuredValueVisible(true);
            gauge.resetMaxMeasuredValue();
            gauge.resetMinMeasuredValue();
        } else {
            gauge.setMaxMeasuredValueVisible(false);
            gauge.setMinMeasuredValueVisible(false);
        }
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        gauge.setGlowing(!gauge.isGlowing());
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        if(jToggleButton4.isSelected()) {
            try {
                double threshold = Double.parseDouble(jTextField1.getText());
                gauge.setThreshold(threshold);
                gauge.setThresholdVisible(true);
            } catch(Exception ex) {}
        } else {
            gauge.setThresholdVisible(false);
        }
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
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
