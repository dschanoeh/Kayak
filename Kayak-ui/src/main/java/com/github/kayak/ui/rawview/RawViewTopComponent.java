/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.rawview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.BusChangeListener;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.Util;
import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.github.kayak.ui.rawview//RawView//EN",
autostore = false)
@TopComponent.Description(preferredID = "RawViewTopComponent",
iconBase="org/freedesktop/tango/16x16/mimetypes/text-x-generic.png", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public final class RawViewTopComponent extends TopComponent {

    private static final Logger logger = Logger.getLogger(RawViewTopComponent.class.getName());
    private Bus bus;
    private Subscription subscription;
    private RawViewTableModel model;
    private SelectionListener selectionListener;
    private ColorRenderer colorRenderer;
    
    private class ColorRenderer extends DefaultTableCellRenderer {
            
        private final Color color = new Color(210, 210, 210);
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component component = super.getTableCellRendererComponent(table, object, isSelected, hasFocus, row, column);
            if(!isSelected) {
                if((row % 2) == 0) {
                    component.setBackground(color);
                } else {
                    component.setBackground(table.getBackground());
                }
            } else {
                component.setBackground(table.getSelectionBackground());
            }
            
            return component;
        }
    };
    
    private BusChangeListener listener = new BusChangeListener() {

        @Override
        public void connectionChanged() {
            
        }

        @Override
        public void nameChanged() {
            setName(NbBundle.getMessage(RawViewTopComponent.class, "CTL_RawViewTopComponent") + " - " + bus.getName());
        }

        @Override
        public void destroyed() {
            close();
        }

        @Override
        public void descriptionChanged() {
            
        }
    };
    
    private class SelectionListener implements ListSelectionListener {
        JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
             if (e.getValueIsAdjusting()) {
                return;
            }
            
            if (e.getSource() == table.getSelectionModel()){
                int row = table.getSelectedRow();
                
                
                if(row != -1) {
                    String idString = (String) model.getValueAt(row, 2);
                    int id = Integer.parseInt(idString.substring(2),16);
                    
                    byte[] bytes = model.getDataForID(id);
                    
                    StringBuilder sb = new StringBuilder();
                    
                    sb.append("ID: ");
                    sb.append(idString);
                    sb.append(" | ");
                 
                    for(byte b : bytes) {
                        sb.append(Util.hexStringToBinaryString(Util.byteToHexString(b)));
                        sb.append(' ');
                    }
                    sb.deleteCharAt(sb.length()-1);
                    jTextField2.setText(sb.toString());
                } else {
                    jTextField2.setText("");
                }
            }
        }
    };

    public RawViewTopComponent() {
        model = new RawViewTableModel();
        initComponents();
        selectionListener = new SelectionListener(jTable1);
        colorRenderer = new ColorRenderer();
        jTable1.setDefaultRenderer(String.class, colorRenderer);
        jTable1.setDefaultRenderer(Integer.class, colorRenderer);
        jTable1.getSelectionModel().addListSelectionListener(selectionListener);
        setName(NbBundle.getMessage(RawViewTopComponent.class, "CTL_RawViewTopComponent"));
        setToolTipText(NbBundle.getMessage(RawViewTopComponent.class, "HINT_RawViewTopComponent"));
    }
    
    private void filter(String filterString) {
        subscription.setSubscribeAll(Boolean.FALSE);
        model.clear();
        String[] idStrings = filterString.split("\\s");

        for (int i = 0; i < idStrings.length; i++) {
            try {
                if (idStrings[i].matches("0x[a-fA-F0-9]+")) {
                    subscription.subscribe(Integer.parseInt(idStrings[i].substring(2), 16));
                } else if (idStrings[i].matches("[a-fA-F0-9]+")) {
                    subscription.subscribe(Integer.parseInt(idStrings[i], 16));
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error while parsing filter string", ex);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jToggleButton1 = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField2 = new javax.swing.JTextField();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(32767, 31));

        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton1, org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jToggleButton1.text")); // NOI18N
        jToggleButton1.setToolTipText(org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jToggleButton1.toolTipText")); // NOI18N
        jToggleButton1.setFocusable(false);
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButton1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        add(jToolBar1);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jLabel1.text")); // NOI18N
        jPanel1.add(jLabel1);

        jTextField1.setText(org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jTextField1.text")); // NOI18N
        jTextField1.setToolTipText(org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jTextField1.toolTipText")); // NOI18N
        jTextField1.setMaximumSize(new java.awt.Dimension(2147483647, 31));
        jPanel1.add(jTextField1);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jCheckBox1.toolTipText")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox1);

        add(jPanel1);

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        jTable1.setModel(model);
        jTable1.setDoubleBuffered(true);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(30);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(20);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(15);
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(160);
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1);

        jTextField2.setEditable(false);
        jTextField2.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextField2.setText(org.openide.util.NbBundle.getMessage(RawViewTopComponent.class, "RawViewTopComponent.jTextField2.text")); // NOI18N
        add(jTextField2);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed

        if(jCheckBox1.isSelected()) {
            filter(jTextField1.getText());
        } else {
            subscription.clear();
            subscription.setSubscribeAll(Boolean.TRUE);
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if(jToggleButton1.isSelected())
            model.setColorized(true);
        else
            model.setColorized(false);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        model.clear();
    }//GEN-LAST:event_jButton1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int[] rows = jTable1.getSelectedRows();
        
        if(rows.length == 0)
            return;
        
        StringBuilder sb = new StringBuilder();
        
        for(int i=0;i<rows.length;i++) {
            String id = (String) model.getValueAt(rows[i], 2);
            sb.append(id);
            if(i != rows.length-1)
                sb.append(" ");
        }
        
        jTextField1.setText(sb.toString());
        jToggleButton1.setSelected(true);
        filter(jTextField1.getText());
}//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        if(subscription != null && bus != null)
            subscription.Terminate();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    public void setBus(Bus bus) {
        this.bus = bus;
        setName(NbBundle.getMessage(RawViewTopComponent.class, "CTL_RawViewTopComponent") + " - " + bus.getName());
        bus.addBusChangeListener(listener);

        subscription = new Subscription(model, bus);
        subscription.setSubscribeAll(Boolean.TRUE);
    }
}
