/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.gui.swing;

import gr.uoi.cs.daintiness.hecate.differencedetection.DifferencesResult;
import gr.uoi.cs.daintiness.hecate.differencedetection.SchemataDifferencesManager;
import gr.uoi.cs.daintiness.hecate.differencedetection.DifferencesAlgorithm;
import gr.uoi.cs.daintiness.hecate.parser.HecateParser;
import gr.uoi.cs.daintiness.hecate.sql.Schema;

import java.io.File;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 * @author iskoulis
 *
 */
public class DiffWorker extends SwingWorker<DifferencesResult, Integer> {
	
	MainPanel mainpanel;
	ProgressMonitor progressmonitor;
	File oldFile = null;
	File newFile = null;
	File folder = null;
	Schema oldSchema;
	Schema newSchema;
	
	public DiffWorker(MainPanel mainpanel,
			          File oldFile, File newFile) {
		this.mainpanel = mainpanel;
		this.oldFile = oldFile;
		this.newFile = newFile;
	}
	
	public DiffWorker(MainPanel mainpanel, File folder) {
		this.mainpanel = mainpanel;
		this.folder = folder;
	}

	@Override
	protected DifferencesResult doInBackground() throws Exception {
		SchemataDifferencesManager differencesManager = new SchemataDifferencesManager();
		
		progressmonitor = new ProgressMonitor(mainpanel.getRootPane(), "Working...", null, 0, 100);
		DifferencesResult result = new DifferencesResult();
		
		if (oldFile != null && newFile != null) {

			oldSchema = HecateParser.parse(oldFile.getAbsolutePath());
			newSchema = HecateParser.parse(newFile.getAbsolutePath());

			
			result = DifferencesAlgorithm.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);
			
		} else if (folder != null){
			/* Here i want to export everything
			 * by checking all the schematas for differences
			 */

			differencesManager.getDifferencesInSchemataHistoryAndExport(folder);
			oldSchema = HecateParser.parse(folder.getAbsolutePath() + File.separator + folder.list()[0]);
			newSchema = HecateParser.parse(folder.getAbsolutePath() + File.separator + folder.list()[folder.list().length-1]);
			result = DifferencesAlgorithm.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);

		}




		return result;
	}

	

	@Override
	protected void done() {
		mainpanel.drawSchema(oldSchema, "old");
		mainpanel.drawSchema(newSchema, "new");
		progressmonitor.setProgress(progressmonitor.getMaximum());
		super.done();
	}
}
