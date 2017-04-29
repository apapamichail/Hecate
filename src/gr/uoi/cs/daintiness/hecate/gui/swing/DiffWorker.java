/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.gui.swing;

import gr.uoi.cs.daintiness.hecate.io.csvExport;
import gr.uoi.cs.daintiness.hecate.io.xmlExport;
import gr.uoi.cs.daintiness.hecate.differencedetection.DifferencesResult;
import gr.uoi.cs.daintiness.hecate.differencedetection.SchemataDifferencesManager;
import gr.uoi.cs.daintiness.hecate.io.MetricsExport;
import gr.uoi.cs.daintiness.hecate.parser.HecateParser;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

import java.io.File;
import java.util.Map.Entry;

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
		progressmonitor = new ProgressMonitor(mainpanel.getRootPane(), "Working...", null, 0, 100);
		DifferencesResult result = new DifferencesResult();
		if (oldFile != null && newFile != null) {

			progressmonitor.setMaximum(3);
			oldSchema = HecateParser.parse(oldFile.getAbsolutePath());
			progressmonitor.setProgress(1);
			newSchema = HecateParser.parse(newFile.getAbsolutePath());
			progressmonitor.setProgress(2);
			result = SchemataDifferencesManager.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);
			progressmonitor.setProgress(3);
			oldFile = null;
			newFile = null;
		} else if (folder != null){

			result.clear();
			Transitions transitions = new Transitions();
			String[] folders = folder.list();

			
			progressmonitor.setMaximum(folders.length);
			String path = folder.getAbsolutePath();
			java.util.Arrays.sort(folders);
			MetricsExport.initMetrics(path);
			for (int i = 0; i < folders.length-1; i++) {

				progressmonitor.setNote("Parsing " + folders[i]);
				Schema schemaA = HecateParser.parse(path + File.separator + folders[i]);
				for (Entry<String, Table> e : schemaA.getTables().entrySet()) {

					String tablename = e.getKey();
					int attributes = e.getValue().getSize();
					result.tablesInfo.addTable(tablename, i, attributes);
				}
				progressmonitor.setNote("Parsing " + folders[i+1]);
				Schema schemaB = HecateParser.parse(path + File.separator + folders[i+1]);
				if (i == folders.length-2) {
					for (Entry<String, Table> e : schemaB.getTables().entrySet()) {
						String tablename = e.getKey();
						int attributes = e.getValue().getSize();
						result.tablesInfo.addTable(tablename, i+1, attributes);
					}
				}
				progressmonitor.setNote(folders[i] + "-" + folders[i+1]);
				result = SchemataDifferencesManager.getDifferencesBetweenTwoSchemata(schemaA, schemaB);
				transitions.add(result.myTransformationList);
				MetricsExport.metrics(result, path);
				progressmonitor.setProgress(i+1);
			}
			try {
				csvExport.tablesCSV(path, result.myMetrics.getNumRevisions()+1, result.tablesInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			xmlExport.xml(transitions, path);
			oldSchema = HecateParser.parse(path + File.separator + folders[0]);
			newSchema = HecateParser.parse(path + File.separator + folders[folders.length-1]);
			result = SchemataDifferencesManager.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);
			folder = null;
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
