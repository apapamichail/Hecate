package gr.uoi.cs.daintiness.hecate.io;

import java.io.File;

public abstract class Export implements ExportInterface{

	public static String getDir(String path) {
		String parrent = (new File(path)).getParent();
		File directory = new File(parrent + File.separator + "results");
		if (!directory.exists()) {
			directory.mkdir();
		}
		return directory.getPath();
	}


}
