/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.rawview;

import com.github.kayak.core.Bus;
import com.github.kayak.core.BusChangeListener;
import com.github.kayak.core.Subscription;
import com.github.kayak.core.Util;
import com.github.kayak.ui.projects.ProjectManager;
import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//com.github.kayak.ui.rawview//RawView//EN",
autostore = false)
@TopComponent.Description(preferredID = "RawViewTopComponent",
iconBase="org/tango-project/tango-icon-theme/16x16/mimetypes/text-x-generic.png",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public final class RawViewTopComponent extends TopComponent {

    private static final Logger logger = Logger.getLogger(RawViewTopComponent.class.getName());

    private Bus bus;
    private Subscription subscription;
    private RawViewTableModel model;
    private SelectionListener selectionListener;

    private class ColorRenderer extends DefaultTableCellRenderer {

        private final Color color = new Color(230, 230, 230);
        private int alignment;

        public ColorRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel component = (JLabel) super.getTableCellRendererComponent(table, object, isSelected, hasFocus, row, column);
            if(!isSelected) {
                if((row % 2) == 0) {
                    component.setBackground(color);
                } else {
                    component.setBackground(table.getBackground());
                }
            } else {
                component.setBackground(table.getSelectionBackground());
            }
            component.setHorizontalAlignment(alignment);
            return component;
        }
    };

    private BusChangeListener listener = new BusChangeListener() {

        @Override
        public void connectionChanged() {

        }

        @Override
        public void nameChanged(String name) {
            setName(NbBundle.getMessage(RawViewTopComponent.class, "CTL_RawViewTopComponent") + " - " + bus.toString());
        }

        @Override
        public void destroyed() {
            close();
        }

        @Override
        public void descriptionChanged() {

        }

        @Override
        public void aliasChanged(String string) {
            setName(NbBundle.getMessage(RawViewTopComponent.class, "CTL_RawViewTopComponent") + " - " + bus.toString());
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
                    row = table.convertRowIndexToModel(row);
                    String idString = (String) model.getValueAt(row, 2);

                    byte[] bytes = model.getDataForID(idString);

                    StringBuilder sb = new StringBuilder(100);

                    sb.append("[ID: ");
                    sb.append(idString);
                    if(idString.length()==8)
                        sb.append(" (extended)");
                    else
                        sb.append(" (standard)");
                    sb.append("] [Timestamp: ");
                    sb.append((String) model.getValueAt(row, 0));

                    sb.append("]\nBinary: ");

                    for(byte b : bytes) {
                        sb.append(Util.hexStringToBinaryString(Util.byteToHexString(b)));
                        sb.append(' ');
                    }
                    sb.setLength(sb.length()-1);

                    sb.append("\nHex:       ");
                    for(byte b : bytes) {
                        sb.append(Util.byteToHexString(b));
                        sb.append("       ");
                    }
                    sb.setLength(sb.length()-7);

                    sb.append("\nASCII:      ");
                    for(byte b : bytes) {
                        sb.append((char) b);
                        sb.append("        ");
                    }
                    sb.setLength(sb.length()-8);


                    jTextArea1.setText(sb.toString());
                } else {
                    jTextArea1.setText("");
                }
            }
        }
    };

    public RawViewTopComponent() {
        model = new RawViewTableModel();
        initComponents();
        selectionListener = new SelectionListener(jTable1);
        ColorRenderer rightColorRenderer = new ColorRenderer(JLabel.RIGHT);
        ColorRenderer leftColorRenderer = new ColorRenderer(JLabel.LEFT);

        TableColumnModel cm = jTable1.getColumnModel();

        cm.getColumn(0).setCellRenderer(rightColorRenderer);
        cm.getColumn(1).setCellRenderer(rightColorRenderer);
        cm.getColumn(2).setCellRenderer(rightColorRenderer);
        cm.getColumn(3).setCellRenderer(rightColorRenderer);
        cm.getColumn(4).setCellRenderer(leftColorRenderer);

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
                if (idStrings[i].matches("[a-fA-F0-9]{3}")) {
                    subscription.subscribe(Integer.parseInt(idStrings[i], 16), false);
                } else if (idStrings[i].matches("[a-fA-F0-9]{8}")) {
                    subscription.subscribe(Integer.parseInt(idStrings[i], 16), true);
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

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

        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(4);
        jTextArea1.setTabSize(4);
        jTextArea1.setMinimumSize(new java.awt.Dimension(100, 200));
        jTextArea1.setPreferredSize(new java.awt.Dimension(100, 200));
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
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
            String id = (String) model.getValueAt(jTable1.convertRowIndexToModel(rows[i]), 2);
            sb.append(id);
            if(i != rows.length-1)
                sb.append(" ");
        }

        jTextField1.setText(sb.toString());
        jCheckBox1.setSelected(true);
        filter(jTextField1.getText());
}//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        if(subscription != null && bus != null)
            subscription.Terminate();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        model.stopRefresh();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        model.startRefresh();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");

        p.setProperty("busName", bus.getName());
        ProjectManager manager = ProjectManager.getGlobalProjectManager();
        p.setProperty("projectName", manager.getOpenedProject().getName());
        p.setProperty("filterString", jTextField1.getText());
        p.setProperty("filterEnabled", Boolean.toString(jCheckBox1.isSelected()));
        p.setProperty("colorized", Boolean.toString(jToggleButton1.isSelected()));

    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");

        String busName = p.getProperty("busName");
        String projectName = p.getProperty("projectName");
        String filterString = p.getProperty("filterString", "");
        String filterEnabled = p.getProperty("filterEnabled", "false");
        String colorized = p.getProperty("colorized", "false");

        logger.log(Level.INFO, "Trying to restore raw view with project {0} and bus {1}", new String[]{projectName, busName});


        Bus newBus = ProjectManager.getGlobalProjectManager().findBus(projectName, busName);

        if (newBus == null) {
            this.close();
            return;
        }

        setBus(newBus);

        jTextField1.setText(filterString);

        if(Boolean.parseBoolean(filterEnabled)) {
            jCheckBox1.setSelected(true);
        }

        if(Boolean.parseBoolean(colorized)) {
            jToggleButton1.setSelected(true);
        }
    }

    public void setBus(Bus bus) {
        this.bus = bus;
        setName(NbBundle.getMessage(RawViewTopComponent.class, "CTL_RawViewTopComponent") + " - " + bus.toString());
        bus.addBusChangeListener(listener);

        subscription = new Subscription(model, bus);
        subscription.setSubscribeAll(Boolean.TRUE);
    }
}
