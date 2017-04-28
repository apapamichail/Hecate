package gr.uoi.cs.daintiness.hecate.differencedetection;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import gr.uoi.cs.daintiness.hecate.io.MetricsExport;
import gr.uoi.cs.daintiness.hecate.io.csvExport;
import gr.uoi.cs.daintiness.hecate.io.xmlExport;
import gr.uoi.cs.daintiness.hecate.parser.HecateParser;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

public class SchemataDifferencesManager {
	
	public DifferencesResult getDifferencesBetweenTwoRevisions(File oldFile, File newFile) {
		DifferencesResult result;
		Schema oldSchema = HecateParser.parse(oldFile.getAbsolutePath());
		Schema newSchema = HecateParser.parse(newFile.getAbsolutePath());
		result = DifferencesAlgorithm.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);
		oldFile = null;
		newFile = null;
		return result;
	}
	/**
	 * @param result
	 * @param folder 
	 * @return
	 * @throws IOException
	 */
	public DifferencesResult getDifferencesInSchemataHistoryAndExport(File folder) throws IOException {
		DifferencesResult result = new DifferencesResult();
		Transitions transitions = new Transitions();
		String[] folders = folder.list();


		String path = folder.getAbsolutePath();
		java.util.Arrays.sort(folders);

		MetricsExport.initMetrics(path);
		
		result.clear();
		
		for (int i = 0; i < folders.length-1; i++) {
			//result.clear();
			Schema schemaA = HecateParser.parse(path + File.separator + folders[i]);
			
			for (Entry<String, Table> e : schemaA.getTables().entrySet()) {

				String tablename = e.getKey();
				int attributes = e.getValue().getSize();
				result.tablesInfo.addTable(tablename, i, attributes);
			}
			
			Schema schemaB = HecateParser.parse(path + File.separator + folders[i+1]);
			if (i == folders.length-2) {
				for (Entry<String, Table> e : schemaB.getTables().entrySet()) {
					String tablename = e.getKey();
					int attributes = e.getValue().getSize();
					result.tablesInfo.addTable(tablename, i+1, attributes);
				}
			}
			
			result = DifferencesAlgorithm.getDifferencesBetweenTwoSchemata(schemaA, schemaB);
			
			transitions.add(result.myTransformationList);
			
			MetricsExport.metrics(result, path);
			
		}
		try {
			csvExport.tables(path, result.myMetrics.getNumRevisions()+1, result.tablesInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xmlExport.xml(transitions, path);
		
		Schema oldSchema = HecateParser.parse(path + File.separator + folders[0]);
		Schema newSchema = HecateParser.parse(path + File.separator + folders[folders.length-1]);
		result.clear();
		result = DifferencesAlgorithm.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);

		return result;
	}

}
