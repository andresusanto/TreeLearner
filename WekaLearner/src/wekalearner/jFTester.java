/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package wekalearner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Debug.Random;
import weka.core.Instances;

/**
 *
 * @author Andre
 */
public class jFTester extends javax.swing.JFrame {

    private Instances training;
    private Classifier model;
    
    public jFTester(Instances training, Classifier model) {
        initComponents();
        this.training = training;
        this.model = model;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setSelected(true);
        jRadioButton3.setText("Supplied dataset");
        jRadioButton3.setActionCommand("1");

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText("10-fold validation");
        jRadioButton4.setActionCommand("2");

        jLabel1.setText("Test method");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Percentage split");
        jRadioButton1.setActionCommand("3");

        jLabel2.setText("Log");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Test");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton4)
                            .addComponent(jRadioButton3)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jRadioButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Evaluation evaluation;
        Instances testset;
        jDOpen openDialog;
        
        if (training == null){
            jTextArea1.append("Please select trainingset...\n");
            openDialog = new jDOpen(this, true);
            openDialog.show();
            if (openDialog.fileName == ""){
                jTextArea1.append("Operation cancelled: No file selected!\n");
                return;
            }
            try {
                BufferedReader reader = new BufferedReader(new FileReader(openDialog.fileName));
                training = new Instances(reader);
                training.setClassIndex(training.numAttributes() - 1);

                jTextArea1.append("Loaded train dataset: ");
                jTextArea1.append(openDialog.fileName);
                jTextArea1.append("\n");

            } catch (Exception ex) {
                jTextArea1.append("Operation cancelled: Invalid dataset file!\n");
                return;
            }
        }
        
        try {
            evaluation = new Evaluation(training);
        } catch (Exception ex) {
            jTextArea1.append("Error in creating evaluation!\n");
            return;
        }
        
        switch(buttonGroup1.getSelection().getActionCommand()){
            case "1": 
                jTextArea1.append("Beginning supplied dataset test...\nPlease select test dataset ...\n");
                openDialog = new jDOpen(this, true);
                openDialog.show();
                if (openDialog.fileName == ""){
                    jTextArea1.append("Operation cancelled: No file selected!\n");
                    return;
                }
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(openDialog.fileName));
                    testset = new Instances(reader);
                    testset.setClassIndex(testset.numAttributes() - 1);

                    jTextArea1.append("Loaded test dataset: ");
                    jTextArea1.append(openDialog.fileName);
                    jTextArea1.append("\n");
                    
                } catch (Exception ex) {
                    jTextArea1.append("Operation cancelled: Invalid dataset file!\n");
                    return;
                }
                
                try {
                    evaluation.evaluateModel(model, testset);
                    jTextArea1.append(evaluation.toSummaryString());
                    jTextArea1.append("\n");
                    jTextArea1.append(evaluation.toMatrixString());
                    jTextArea1.append("\n");
                    
                } catch (Exception ex) {
                    jTextArea1.append("Operation cancelled: Error in evaluating model!\n");
                }
                break;
            case "2":
                jTextArea1.append("Beginning 10-fold validation test...\n");
                
                try {
                    evaluation.crossValidateModel(model, training, 10, new Random(1));
                    jTextArea1.append(evaluation.toSummaryString());
                    jTextArea1.append("\n");
                    jTextArea1.append(evaluation.toMatrixString());
                    jTextArea1.append("\n");
                    
                } catch (Exception ex) {
                    jTextArea1.append("Operation cancelled: Error in evaluating model!\n");
                }
                break;
            case "3":
                jTextArea1.append("Beginning percentage split test...\n");
                String percent = JOptionPane.showInputDialog("Please input percentage to split: ");
                float percentage = Float.parseFloat(percent) / 100.0f;
                
                try {
                    int trainSize = (int) Math.round(training.numInstances() * percentage);
                    int testSize = training.numInstances() - trainSize;
                    Instances train = new Instances(training, 0, trainSize);
                    Instances test = new Instances(training, trainSize, testSize);
                    model.buildClassifier(train);
                    
                    evaluation.evaluateModel(model, test);
                    
                    jTextArea1.append(evaluation.toSummaryString());
                    jTextArea1.append("\n");
                    jTextArea1.append(evaluation.toMatrixString());
                    jTextArea1.append("\n");
                    
                } catch (Exception ex) {
                    jTextArea1.append("Operation cancelled: Error in evaluating model!\n" + ex.getMessage());
                }
                break;
        }
        
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
