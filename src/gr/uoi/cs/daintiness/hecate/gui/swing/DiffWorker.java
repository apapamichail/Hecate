/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.gui.swing;

import gr.uoi.cs.daintiness.hecate.diff.SchemataDifferencesManager;
import gr.uoi.cs.daintiness.hecate.diff.DifferencesResult;
import gr.uoi.cs.daintiness.hecate.io.csvExport;
import gr.uoi.cs.daintiness.hecate.io.xmlExport;
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
			String[] list = folder.list();

			
			progressmonitor.setMaximum(list.length);
			String path = folder.getAbsolutePath();
			java.util.Arrays.sort(list);
			MetricsExport.initMetrics(path);
			for (int i = 0; i < list.length-1; i++) {

				progressmonitor.setNote("Parsing " + list[i]);
				Schema schema = HecateParser.parse(path + File.separator + list[i]);
				for (Entry<String, Table> e : schema.getTables().entrySet()) {

					String tablename = e.getKey();
					int attributes = e.getValue().getSize();
					result.tablesInfo.addTable(tablename, i, attributes);
				}
				progressmonitor.setNote("Parsing " + list[i+1]);
				Schema schema2 = HecateParser.parse(path + File.separator + list[i+1]);
				if (i == list.length-2) {
					for (Entry<String, Table> e : schema2.getTables().entrySet()) {
						String tablename = e.getKey();
						int attributes = e.getValue().getSize();
						result.tablesInfo.addTable(tablename, i+1, attributes);
					}
				}
				progressmonitor.setNote(list[i] + "-" + list[i+1]);
				result = SchemataDifferencesManager.getDifferencesBetweenTwoSchemata(schema, schema2);
				transitions.add(result.myTransformationList);
				MetricsExport.metrics(result, path);
				progressmonitor.setProgress(i+1);
			}
			try {
				csvExport.tables(path, result.myMetrics.getNumRevisions()+1, result.tablesInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			xmlExport.xml(transitions, path);
			oldSchema = HecateParser.parse(path + File.separator + list[0]);
			newSchema = HecateParser.parse(path + File.separator + list[list.length-1]);
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
