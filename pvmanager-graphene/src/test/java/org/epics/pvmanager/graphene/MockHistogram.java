/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.pvmanager.graphene;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import org.epics.pvmanager.CompositeDataSource;
import org.epics.pvmanager.jca.JCADataSource;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.graphene.AreaGraph2DRendererUpdate;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import org.epics.vtype.ValueUtil;
import org.epics.vtype.VImage;
import org.epics.pvmanager.sim.SimulationDataSource;
import static org.epics.pvmanager.vtype.ExpressionLanguage.*;
import static org.epics.pvmanager.util.Executors.*;
import static org.epics.util.time.TimeDuration.*;

/**
 *
 * @author carcassi
 */
public class MockHistogram extends javax.swing.JFrame {

    /**
     * Creates new form MockWaterfallPlot
     */
    public MockHistogram() {
        PVManager.setDefaultNotificationExecutor(swingEDT());
        CompositeDataSource dataSource = new CompositeDataSource();
        dataSource.putDataSource("sim", SimulationDataSource.simulatedData());
        dataSource.putDataSource("epics", new JCADataSource());
        dataSource.setDefaultDataSource("sim");
        PVManager.setDefaultDataSource(dataSource);
        initComponents();
        plotView.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                if (plot != null) {
                    plot.update(new AreaGraph2DRendererUpdate().imageHeight(plotView.getHeight()).imageWidth(plotView.getWidth()));
                }
            }
        });
    }
    private PVReader<Graph2DResult> pv;
    private AreaGraph2DExpression plot;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        pvName = new javax.swing.JTextField();
        lastError = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        plotView = new org.epics.pvmanager.graphene.ImagePanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("PV Name:");

        pvName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pvNameActionPerformed(evt);
            }
        });

        lastError.setEditable(false);

        javax.swing.GroupLayout plotViewLayout = new javax.swing.GroupLayout(plotView);
        plotView.setLayout(plotViewLayout);
        plotViewLayout.setHorizontalGroup(
            plotViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        plotViewLayout.setVerticalGroup(
            plotViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 122, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pvName, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(plotView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lastError))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(pvName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(60, 60, 60)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lastError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pvNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pvNameActionPerformed
        if (pv != null) {
            pv.close();
            plotView.setImage(null);
        }
        
        if (pvName.getText() == null || pvName.getText().trim().isEmpty()) {
            return;
        }

        plot = ExpressionLanguage.histogramOf(vNumber(pvName.getText()));
        plot.update(new AreaGraph2DRendererUpdate().imageHeight(plotView.getHeight()).imageWidth(plotView.getWidth()));
        pv = PVManager.read(plot)
                .notifyOn(swingEDT())
                .readListener(new PVReaderListener<Graph2DResult>() {

                    @Override
                    public void pvChanged(PVReaderEvent<Graph2DResult> event) {
                        setLastError(pv.lastException());
                        if (pv.getValue() != null) {
                            BufferedImage image = ValueUtil.toImage(pv.getValue().getImage());
                            plotView.setImage(image);
                        }
                    }
                })
                .maxRate(ofHertz(50));
    }//GEN-LAST:event_pvNameActionPerformed

    private void setLastError(Exception ex) {
        if (ex != null) {
            lastError.setText(ex.getMessage());
            Logger.getLogger(MockHistogram.class.getName()).log(Level.WARNING, "Error", ex);
        } else {
            lastError.setText("");
        }
    }
    final BufferedImage finalBuffer = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MockHistogram().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField lastError;
    private org.epics.pvmanager.graphene.ImagePanel plotView;
    private javax.swing.JTextField pvName;
    // End of variables declaration//GEN-END:variables
}
