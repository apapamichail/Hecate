package gr.uoi.cs.daintiness.hecate.gui.swing;
import gr.uoi.cs.daintiness.hecate.Hecate;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class InstructionsDialog extends JDialog {
	private JButton close;
	private JLabel generalInformation;
	private JLabel informationRED;
	private JLabel informationGREEN;
	private JLabel informationYELLOW;
	private JLabel informationWHITE;
	
	public InstructionsDialog() {
		initialize();
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(Box.createRigidArea(new Dimension(0, 15)));
		generalInformation = new JLabel("<html>This software compares database schemata. It works on two modes:<br><br> 1.Compares and finds the differences between two schemata and saves metrics.<br><br>2. Compares and finds the differences of all theschemata of a database during its lifetime.</html>");
		generalInformation.setFont(new Font("Calibri", Font.BOLD, 14));
		generalInformation.setAlignmentX(CENTER_ALIGNMENT);
		add(generalInformation);
		
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(Box.createRigidArea(new Dimension(0, 15)));
		informationRED = new JLabel("RED COLOURED COLUMN MEANS DELETION");
		informationRED.setFont(new Font("Calibri", Font.BOLD, 14));
		informationRED.setAlignmentX(CENTER_ALIGNMENT);
		add(informationRED);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		informationGREEN = new JLabel("GREEN COLOURED COLUMN MEANS ADDITION");
		informationGREEN.setFont(new Font("Calibri", Font.BOLD, 14));
		informationGREEN.setAlignmentX(CENTER_ALIGNMENT);
		add(informationGREEN);
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		informationYELLOW = new JLabel("YELLOW COLOURED COLUMN MEANS ALTERATION");
		informationYELLOW.setFont(new Font("Calibri", Font.BOLD, 14));
		informationYELLOW.setAlignmentX(CENTER_ALIGNMENT);
		add(informationYELLOW);
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		informationWHITE = new JLabel("WHITE COLOURED COLUMN MEANS NO CHANGES");
		informationWHITE.setFont(new Font("Calibri", Font.BOLD, 14));
		informationWHITE.setAlignmentX(CENTER_ALIGNMENT);
		add(informationWHITE);
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		close.setAlignmentX(CENTER_ALIGNMENT);
		add(close);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		draw();
	}
	
	private void initialize() {
		setTitle("About");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private void draw() {
		pack();
		setResizable(false);
		// center on screen
		Toolkit toolkit = getToolkit();
		Dimension size = toolkit.getScreenSize();
		setLocation(size.width/2 - getWidth()/2, 
		            size.height/2 - getHeight()/2);
	}
}
