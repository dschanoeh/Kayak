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
package com.github.kayak.ui.send;

import com.github.kayak.core.Bus;
import com.github.kayak.core.Util;
import com.github.kayak.ui.projects.Project;
import com.github.kayak.ui.projects.ProjectChangeListener;
import com.github.kayak.ui.projects.ProjectManagementListener;
import com.github.kayak.ui.projects.ProjectManager;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
@ConvertAsProperties(dtd = "-//com.github.kayak.ui.send//SendFrames//EN",
autostore = false)
@TopComponent.Description(preferredID = "SendFramesTopComponent",
iconBase="org/tango-project/tango-icon-theme/16x16/actions/mail-forward.png",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SendFramesAction",
preferredID = "SendFramesTopComponent")
public final class SendFramesTopComponent extends TopComponent {

    private static final Logger logger = Logger.getLogger(SendFramesTopComponent.class.getCanonicalName());

    private Project project;
    private SendFramesTableModel tableModel = new SendFramesTableModel();

    private ProjectManagementListener managementListener = new ProjectManagementListener() {

            @Override
            public void projectsUpdated() {

            }

            @Override
            public void openProjectChanged(Project p) {
                p.addProjectChangeListener(projectListener);
                fillComboBox();
            }
        };

    private ProjectChangeListener projectListener = new ProjectChangeListener() {

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
            fillComboBox();
        }

        @Override
        public void projectBusRemoved(Project p, Bus bus) {
            fillComboBox();
        }
    };

    public SendFramesTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(SendFramesTopComponent.class, "CTL_SendFramesTopComponent"));
        setToolTipText(NbBundle.getMessage(SendFramesTopComponent.class, "HINT_SendFramesTopComponent"));

        ProjectManager.getGlobalProjectManager().addListener(managementListener);
        project = ProjectManager.getGlobalProjectManager().getOpenedProject();
        if(project != null) {
            project.addProjectChangeListener(projectListener);
            fillComboBox();
        }

        Action send = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                ((SendFramesTableModel) table.getModel()).send(modelRow);
            }
        };

        ButtonColumn bc = new ButtonColumn(jTable1, send, 4);

        jTable1.getColumn("Bus").setPreferredWidth(100);
        jTable1.getColumn("ID [hex]").setPreferredWidth(60);
        jTable1.getColumn("Length").setPreferredWidth(70);
        jTable1.getColumn("Data").setPreferredWidth(200);
        jTable1.getColumn("Send").setPreferredWidth(60);
        jTable1.getColumn("Interval [ms]").setPreferredWidth(100);
        jTable1.getColumn("Send interval").setPreferredWidth(100);
        jTable1.getColumn("Note").setPreferredWidth(150);
    }

    private void fillComboBox() {
        jComboBox1.removeAllItems();
        Project p = ProjectManager.getGlobalProjectManager().getOpenedProject();

        for(Bus b : p.getBusses()) {
            jComboBox1.addItem(b);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();

        jTable1.setModel(tableModel);
        jScrollPane1.setViewportView(jTable1);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SendFramesTopComponent.class, "SendFramesTopComponent.jLabel1.text")); // NOI18N
        jToolBar1.add(jLabel1);

        jToolBar1.add(jComboBox1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SendFramesTopComponent.class, "SendFramesTopComponent.jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);
        jToolBar1.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(SendFramesTopComponent.class, "SendFramesTopComponent.jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            Bus b = (Bus) jComboBox1.getSelectedItem();

            if(b != null)
                tableModel.add(b);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "No bus was selected");
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        tableModel.remove(jTable1.getSelectedRow());
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        ProjectManager.getGlobalProjectManager().removeListener(managementListener);
    }

    void writeProperties(java.util.Properties p) {
        int rowCount = tableModel.getRowCount();

        p.setProperty("version", "1.0");
        p.setProperty("rowCount", Integer.toString(rowCount));

        for(int i=0;i<rowCount;i++) {
            SendFramesTableModel.TableRow row = tableModel.getRow(i);
            String is = Integer.toString(i);
            p.setProperty("note" + is, row.getNote());
            p.setProperty("busName" + is, row.getBus().getName());
            p.setProperty("data" + is, Util.byteArrayToHexString(row.getData(), false));
            p.setProperty("id" + is, Integer.toString(row.getId()));
            p.setProperty("interval" + is, Integer.toString(row.getInterval()));
            p.setProperty("projectName", project.getName());
        }
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");

        int rowCount = Integer.parseInt(p.getProperty("rowCount"));
        String projectName = p.getProperty("projectName");

        Project pr = ProjectManager.getGlobalProjectManager().getOpenedProject();
        if(pr == null || !pr.getName().equals(projectName))
            return;

        for(int i=0;i<rowCount;i++) {
            String is = Integer.toString(i);

            String busName = p.getProperty("busName" + is);
            Bus bus = ProjectManager.getGlobalProjectManager().findBus(projectName, busName);

            if(bus == null)
                continue;

            String note = p.getProperty("note" + is);
            int id = Integer.parseInt(p.getProperty("id" + is));
            int interval = Integer.parseInt(p.getProperty("interval" + is));
            byte[] data = Util.hexStringToByteArray(p.getProperty("data" + is));

            tableModel.add(bus, id, interval, data, note);

        }


    }
}
