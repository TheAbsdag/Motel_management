/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

/**
 *
 * @author Santiago
 */
public class RoomView extends javax.swing.JPanel {

    /**
     * Creates new form RoomView
     */
    public RoomView() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundRoom = new javax.swing.JPanel();
        roomStatusBackground = new javax.swing.JPanel();
        remainingInformativeLabel = new javax.swing.JLabel();
        roomNumber1 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        startInformativeLabel = new javax.swing.JLabel();
        startTimeLabel = new javax.swing.JLabel();
        remainingTimeLabel1 = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        roomStatusInformative = new javax.swing.JLabel();
        booking3HoursButton = new javax.swing.JButton();
        booking12HoursButton = new javax.swing.JButton();
        booking6HoursButton = new javax.swing.JButton();
        backRoomButton = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        addSmallQuantityButton = new javax.swing.JButton();
        removeSmallQuantityButton = new javax.swing.JButton();
        removeBigQuantity = new javax.swing.JButton();
        addBigQuantityButton = new javax.swing.JButton();
        printingCheckBox = new javax.swing.JCheckBox();
        endTimeButton = new javax.swing.JButton();
        addTimeButton = new javax.swing.JButton();
        roomSellingButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        roomStatusBackground.setBackground(new java.awt.Color(102, 255, 0));

        remainingInformativeLabel.setFont(new java.awt.Font("Segoe UI Black", 0, 70)); // NOI18N
        remainingInformativeLabel.setForeground(new java.awt.Color(0, 0, 0));
        remainingInformativeLabel.setText("RESTANTE");

        roomNumber1.setFont(new java.awt.Font("Segoe UI Black", 0, 90)); // NOI18N
        roomNumber1.setForeground(new java.awt.Color(0, 0, 0));
        roomNumber1.setText("000");

        statusLabel.setFont(new java.awt.Font("Segoe UI Black", 0, 90)); // NOI18N
        statusLabel.setForeground(new java.awt.Color(0, 0, 0));
        statusLabel.setText("LIMPIEZA");
        statusLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102), 15));

        startInformativeLabel.setFont(new java.awt.Font("Segoe UI Black", 0, 70)); // NOI18N
        startInformativeLabel.setForeground(new java.awt.Color(0, 0, 0));
        startInformativeLabel.setText("INICIO");

        startTimeLabel.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        startTimeLabel.setForeground(new java.awt.Color(51, 51, 51));
        startTimeLabel.setText("00:00 AM");

        remainingTimeLabel1.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        remainingTimeLabel1.setForeground(new java.awt.Color(51, 51, 51));
        remainingTimeLabel1.setText("00:00 ");

        javax.swing.GroupLayout roomStatusBackgroundLayout = new javax.swing.GroupLayout(roomStatusBackground);
        roomStatusBackground.setLayout(roomStatusBackgroundLayout);
        roomStatusBackgroundLayout.setHorizontalGroup(
            roomStatusBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roomStatusBackgroundLayout.createSequentialGroup()
                .addGap(396, 396, 396)
                .addComponent(roomNumber1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(roomStatusBackgroundLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(roomStatusBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roomStatusBackgroundLayout.createSequentialGroup()
                        .addComponent(startTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(731, 731, 731))
                    .addGroup(roomStatusBackgroundLayout.createSequentialGroup()
                        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(roomStatusBackgroundLayout.createSequentialGroup()
                        .addComponent(startInformativeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(remainingInformativeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(128, 128, 128))))
            .addGroup(roomStatusBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roomStatusBackgroundLayout.createSequentialGroup()
                    .addContainerGap(537, Short.MAX_VALUE)
                    .addComponent(remainingTimeLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(205, 205, 205)))
        );
        roomStatusBackgroundLayout.setVerticalGroup(
            roomStatusBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roomStatusBackgroundLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(roomNumber1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(roomStatusBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(remainingInformativeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startInformativeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(startTimeLabel)
                .addGap(131, 131, 131))
            .addGroup(roomStatusBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roomStatusBackgroundLayout.createSequentialGroup()
                    .addContainerGap(520, Short.MAX_VALUE)
                    .addComponent(remainingTimeLabel1)
                    .addGap(121, 121, 121)))
        );

        timeLabel.setFont(new java.awt.Font("Segoe UI Black", 0, 56)); // NOI18N
        timeLabel.setForeground(new java.awt.Color(255, 255, 255));
        timeLabel.setText("00:00 AM ");

        roomStatusInformative.setFont(new java.awt.Font("Segoe UI Black", 0, 40)); // NOI18N
        roomStatusInformative.setText("SOBRETIEMPO");

        booking3HoursButton.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        booking3HoursButton.setText("3");
        booking3HoursButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                booking3HoursButtonActionPerformed(evt);
            }
        });

        booking12HoursButton.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        booking12HoursButton.setText("12");
        booking12HoursButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                booking12HoursButtonActionPerformed(evt);
            }
        });

        booking6HoursButton.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        booking6HoursButton.setText("6");
        booking6HoursButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                booking6HoursButtonActionPerformed(evt);
            }
        });

        backRoomButton.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        backRoomButton.setText("VOLVER");

        jTextField1.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        jTextField1.setText("100000000");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        addSmallQuantityButton.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        addSmallQuantityButton.setText("+100");

        removeSmallQuantityButton.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        removeSmallQuantityButton.setText("-100");
        removeSmallQuantityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSmallQuantityButtonActionPerformed(evt);
            }
        });

        removeBigQuantity.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        removeBigQuantity.setText("-1000");
        removeBigQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBigQuantityActionPerformed(evt);
            }
        });

        addBigQuantityButton.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        addBigQuantityButton.setText("+1000");
        addBigQuantityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBigQuantityButtonActionPerformed(evt);
            }
        });

        printingCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        printingCheckBox.setSelected(true);
        printingCheckBox.setText("IMPRESION");

        endTimeButton.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        endTimeButton.setText("TERMINAR");

        addTimeButton.setFont(new java.awt.Font("Segoe UI Black", 0, 48)); // NOI18N
        addTimeButton.setText("VENDER");

        roomSellingButton.setFont(new java.awt.Font("Segoe UI Black", 0, 24)); // NOI18N
        roomSellingButton.setText("VENTA A LA HABITACION");

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 0, 24)); // NOI18N
        jLabel1.setText("DICIEMBRE 2 2024");

        javax.swing.GroupLayout backgroundRoomLayout = new javax.swing.GroupLayout(backgroundRoom);
        backgroundRoom.setLayout(backgroundRoomLayout);
        backgroundRoomLayout.setHorizontalGroup(
            backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundRoomLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(backgroundRoomLayout.createSequentialGroup()
                        .addComponent(backRoomButton, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(roomSellingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(endTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(roomStatusBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 969, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundRoomLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundRoomLayout.createSequentialGroup()
                                .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(removeBigQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(removeSmallQuantityButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(addBigQuantityButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(addSmallQuantityButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundRoomLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(backgroundRoomLayout.createSequentialGroup()
                                        .addComponent(booking6HoursButton)
                                        .addGap(29, 29, 29)
                                        .addComponent(booking12HoursButton))
                                    .addComponent(roomStatusInformative)
                                    .addComponent(timeLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(15, 15, 15))
                            .addGroup(backgroundRoomLayout.createSequentialGroup()
                                .addComponent(addTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundRoomLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundRoomLayout.createSequentialGroup()
                                .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(booking3HoursButton)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(19, 19, 19))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundRoomLayout.createSequentialGroup()
                                .addComponent(printingCheckBox)
                                .addGap(45, 45, 45)))))
                .addContainerGap())
        );
        backgroundRoomLayout.setVerticalGroup(
            backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundRoomLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundRoomLayout.createSequentialGroup()
                        .addComponent(roomStatusBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 641, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(endTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(backRoomButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(roomSellingButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(backgroundRoomLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roomStatusInformative)
                        .addGap(27, 27, 27)
                        .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(booking3HoursButton)
                            .addComponent(booking12HoursButton)
                            .addComponent(booking6HoursButton))
                        .addGap(18, 18, 18)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(removeSmallQuantityButton)
                            .addComponent(addSmallQuantityButton))
                        .addGap(31, 31, 31)
                        .addGroup(backgroundRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(removeBigQuantity)
                            .addComponent(addBigQuantityButton))
                        .addGap(18, 18, 18)
                        .addComponent(printingCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(272, Short.MAX_VALUE)
                .addComponent(backgroundRoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(backgroundRoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void booking3HoursButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_booking3HoursButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_booking3HoursButtonActionPerformed

    private void booking12HoursButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_booking12HoursButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_booking12HoursButtonActionPerformed

    private void booking6HoursButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_booking6HoursButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_booking6HoursButtonActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void removeSmallQuantityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSmallQuantityButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_removeSmallQuantityButtonActionPerformed

    private void removeBigQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBigQuantityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_removeBigQuantityActionPerformed

    private void addBigQuantityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBigQuantityButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addBigQuantityButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBigQuantityButton;
    private javax.swing.JButton addSmallQuantityButton;
    private javax.swing.JButton addTimeButton;
    private javax.swing.JButton backRoomButton;
    private javax.swing.JPanel backgroundRoom;
    private javax.swing.JButton booking12HoursButton;
    private javax.swing.JButton booking3HoursButton;
    private javax.swing.JButton booking6HoursButton;
    private javax.swing.JButton endTimeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JCheckBox printingCheckBox;
    private javax.swing.JLabel remainingInformativeLabel;
    private javax.swing.JLabel remainingTimeLabel1;
    private javax.swing.JButton removeBigQuantity;
    private javax.swing.JButton removeSmallQuantityButton;
    private javax.swing.JLabel roomNumber1;
    private javax.swing.JButton roomSellingButton;
    private javax.swing.JPanel roomStatusBackground;
    private javax.swing.JLabel roomStatusInformative;
    private javax.swing.JLabel startInformativeLabel;
    private javax.swing.JLabel startTimeLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel timeLabel;
    // End of variables declaration//GEN-END:variables
}
