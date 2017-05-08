package gr.uoi.cs.daintiness.hecate.differencedetection;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import gr.uoi.cs.daintiness.hecate.exportinformation.MetricsExport;
import gr.uoi.cs.daintiness.hecate.exportinformation.csvExport;
import gr.uoi.cs.daintiness.hecate.exportinformation.xmlExport;
import gr.uoi.cs.daintiness.hecate.parser.HecateParser;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

public class SchemataDifferencesManager {
	
	public DifferencesResult getDifferencesBetweenTwoRevisions(Schema oldSchema, Schema newSchema) {
		AlgorithmFactory algorithmFactory = new AlgorithmFactory();
		
		DifferencesAlgorithm differencesAlgorithm = algorithmFactory.getAlgorithm("DifferencesAlgorithmSkoulis");

		DifferencesResult result =new DifferencesResult();
		result.clear();

		result = differencesAlgorithm.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);

		return result;
	}
	
	
	/**
	 * @param result
	 * @param folder 
	 * @return
	 * @throws IOException
	 */
	public DifferencesResult checkDifferencesInSchemataHistoryAndExport(File folder) throws IOException {
		DifferencesResult result = new DifferencesResult();
		AlgorithmFactory algorithmFactory = new AlgorithmFactory();
		Transitions transitions = new Transitions();
		DifferencesAlgorithm differencesAlgorithm = algorithmFactory.getAlgorithm("DifferencesAlgorithmSkoulis");
		
		MetricsExport metricsExport = new MetricsExport();
		csvExport exportToCSV = new csvExport();
		xmlExport exportToXML = new xmlExport();
		
		String[] folders = folder.list();

		String path = folder.getAbsolutePath();
		java.util.Arrays.sort(folders);

		metricsExport.initMetrics(path);
		
		result.clear();
		
		for (int i = 0; i < folders.length-1; i++) {
			//result.clear();
			System.out.println(path + File.separator + folders[i]);
			Schema schemaA = getSchema(path + File.separator + folders[i]);
			
			for (Entry<String, Table> e : schemaA.getTables().entrySet()) {

				String tablename = e.getKey();
				int attributes = e.getValue().getSize();
				result.tablesInfo.addTable(tablename, i, attributes);
			}
			
			Schema schemaB = getSchema(path + File.separator + folders[i+1]);
			if (i == folders.length-2) {
				for (Entry<String, Table> e : schemaB.getTables().entrySet()) {
					String tablename = e.getKey();
					int attributes = e.getValue().getSize();
					result.tablesInfo.addTable(tablename, i+1, attributes);
				}
			}
			
			result = differencesAlgorithm.getDifferencesBetweenTwoSchemata(schemaA, schemaB);
			
			transitions.add(result.myTransformationList);
			
			metricsExport.metrics(result, path);
			
		}
		try {
			exportToCSV.tablesCSV(path, result.myMetrics.getNumRevisions()+1, result.tablesInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportToXML.export(transitions, path);
		
		Schema oldSchema = HecateParser.parse(path + File.separator + folders[0]);
		Schema newSchema = HecateParser.parse(path + File.separator + folders[folders.length-1]);
		result.clear();
		result = differencesAlgorithm.getDifferencesBetweenTwoSchemata(oldSchema, newSchema);

		return result;
	}

	public Schema getSchema(String path){
		return HecateParser.parse(path);
	}
}
