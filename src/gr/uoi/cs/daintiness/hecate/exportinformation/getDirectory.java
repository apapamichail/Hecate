package gr.uoi.cs.daintiness.hecate.exportinformation;

import java.io.File;

public class getDirectory{

	public String getDir(String path) {
		String parrent = (new File(path)).getParent();
		File directory = new File(parrent + File.separator + "results");
		if (!directory.exists()) {
			directory.mkdir();
		}
		return directory.getPath();
	}

}
