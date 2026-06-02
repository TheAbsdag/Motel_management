/*
 * Created by JFormDesigner on Tue Jun 02 16:28:11 GMT-05:00 2026
 */

package view;

import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class EmailView extends JPanel {
    public EmailView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	label1 = new JLabel();
	button1 = new JButton();
	label2 = new JLabel();
	scrollPane1 = new JScrollPane();
	list1 = new JList();
	button2 = new JButton();
	button3 = new JButton();
	label3 = new JLabel();
	textField1 = new JTextField();
	label4 = new JLabel();
	scrollPane2 = new JScrollPane();
	textArea1 = new JTextArea();
	button6 = new JButton();
	button4 = new JButton();
	button5 = new JButton();
	panel1 = new JPanel();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- label1 ----
	label1.setText("CONFIGURACION DE CORREO");
	add(label1, "cell 0 0");

	//---- button1 ----
	button1.setText("CONFIGURAR PROOVEDOR");
	add(button1, "cell 3 0,growy");

	//---- label2 ----
	label2.setText("DESTINATARIOS");
	add(label2, "cell 0 1");

	//======== scrollPane1 ========
	{
	    scrollPane1.setViewportView(list1);
	}
	add(scrollPane1, "cell 1 1");

	//---- button2 ----
	button2.setText("A\u00f1adir");
	add(button2, "cell 2 1");

	//---- button3 ----
	button3.setText("Eliminar");
	add(button3, "cell 2 1");

	//---- label3 ----
	label3.setText("Asunto:");
	add(label3, "cell 0 2");
	add(textField1, "cell 1 2");

	//---- label4 ----
	label4.setText("Cuerpo:");
	add(label4, "cell 0 3");

	//======== scrollPane2 ========
	{
	    scrollPane2.setViewportView(textArea1);
	}
	add(scrollPane2, "cell 1 3 2 3,growy");

	//---- button6 ----
	button6.setText("Auto configurar");
	add(button6, "cell 3 3");

	//---- button4 ----
	button4.setText("VOLVER");
	add(button4, "cell 0 6,growy");

	//---- button5 ----
	button5.setText("GUARDAR");
	add(button5, "cell 3 6,growy");

	//======== panel1 ========
	{
	    panel1.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[fill]" +
		"[fill]",
		// rows
		"[]" +
		"[]" +
		"[]"));
	}
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel label1;
    private JButton button1;
    private JLabel label2;
    private JScrollPane scrollPane1;
    private JList list1;
    private JButton button2;
    private JButton button3;
    private JLabel label3;
    private JTextField textField1;
    private JLabel label4;
    private JScrollPane scrollPane2;
    private JTextArea textArea1;
    private JButton button6;
    private JButton button4;
    private JButton button5;
    private JPanel panel1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
