package gr.uoi.cs.daintiness.hecate;
import java.io.IOException;

import gr.uoi.cs.daintiness.hecate.gui.swing.MainWindow;

/**
 * This is the class that contains the main function
 * for Hecate.
 * @author giskou
 */
public class Hecate{

	private static MainWindow hecate;
	/** A
	 * Creates and shows the main Hecate Window
	 * @param args 
	 */
	public static void main(String[] args)  {
		//test t =new test();
		hecate = new MainWindow();
		hecate.setVisible(true);
	}
}
