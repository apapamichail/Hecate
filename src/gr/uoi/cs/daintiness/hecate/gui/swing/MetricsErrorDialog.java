package gr.uoi.cs.daintiness.hecate.gui.swing;

import gr.uoi.cs.daintiness.hecate.metrics.Metrics;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Font;

public class MetricsErrorDialog extends JDialog{
	private JLabel ErrorMessage;
	private JButton close;
	public MetricsErrorDialog() {
		initialize();
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(Box.createRigidArea(new Dimension(0, 15)));
		ErrorMessage = new JLabel("<html> Metrics not saved. Please choose schemata to parse. </html>");
		ErrorMessage.setFont(new Font("Calibri", Font.BOLD, 14));
		ErrorMessage.setAlignmentX(CENTER_ALIGNMENT);
		add(ErrorMessage);
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
