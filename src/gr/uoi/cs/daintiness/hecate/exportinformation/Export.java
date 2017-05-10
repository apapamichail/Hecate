package gr.uoi.cs.daintiness.hecate.exportinformation;

import java.io.File;

import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

public abstract class Export {

	public static String getDir(String path) {
		String parrent = (new File(path)).getParent();
		File directory = new File(parrent + File.separator + "results");
		if (!directory.exists()) {
			directory.mkdir();
		}
		return directory.getPath();
	}
	
	abstract void export();

	public void export(Transitions transition, String path) {
		// TODO Auto-generated method stub
		
	}


}
