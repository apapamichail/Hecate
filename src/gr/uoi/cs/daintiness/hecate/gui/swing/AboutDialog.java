package gr.uoi.cs.daintiness.hecate.gui.swing;

import gr.uoi.cs.daintiness.hecate.Hecate;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
/**
 * A dialog displaying Hecates information
 * @author giskou
 *
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	
	private JLabel name;
	private JLabel author;
	private JLabel mail;
	private JButton close;
	private JLabel informationRED;
	private JLabel informationGREEN;
	private JLabel informationYELLOW;
	private JLabel informationWHITE;
	private ImageIcon hecateIcon;

	public AboutDialog() {
		initialize();

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		add(Box.createRigidArea(new Dimension(0, 15)));

		name = new JLabel("      Hecate  0.2     ");
		name.setFont(new Font("Serif", Font.BOLD, 15));
		name.setAlignmentX(CENTER_ALIGNMENT);
		add(name);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		hecateIcon = new ImageIcon(Hecate.class.getResource("art/icon.png"));
		JLabel iconLabel = new JLabel(hecateIcon);
		iconLabel.setAlignmentX(0.5f);
		add(iconLabel);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		author = new JLabel("         © Skoulis Ioannis        ");
		author.setFont(new Font("Serif", Font.PLAIN, 13));
		author.setAlignmentX(CENTER_ALIGNMENT);
		add(author);
		mail = new JLabel("        iskoulis@cs.uoi.gr        ");
		mail.setFont(new Font("Serif", Font.PLAIN, 12));
		mail.setAlignmentX(CENTER_ALIGNMENT);
		add(mail);

		add(Box.createRigidArea(new Dimension(0, 15)));
		informationRED = new JLabel("RED COLOUR MEANS DELETION");
		informationRED.setFont(new Font("Calibri", Font.BOLD, 14));
		informationRED.setAlignmentX(CENTER_ALIGNMENT);
		add(informationRED);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		informationGREEN = new JLabel("GREEN COLOUR MEANS ADDITION");
		informationGREEN.setFont(new Font("Calibri", Font.BOLD, 14));
		informationGREEN.setAlignmentX(CENTER_ALIGNMENT);
		add(informationGREEN);
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		informationYELLOW = new JLabel("YELLOW COLOUR MEANS ALTERATION");
		informationYELLOW.setFont(new Font("Calibri", Font.BOLD, 14));
		informationYELLOW.setAlignmentX(CENTER_ALIGNMENT);
		add(informationYELLOW);
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		informationWHITE = new JLabel("WHITE COLOUR MEANS NO CHANGES");
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

