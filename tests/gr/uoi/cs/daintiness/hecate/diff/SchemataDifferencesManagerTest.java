/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.diff;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import org.junit.Before;
import org.junit.Test;


import gr.uoi.cs.daintiness.hecate.differencedetection.SchemataDifferencesManager;



/**
 * @author angeloASDA
 * 
 *
 */
public class SchemataDifferencesManagerTest {
	File schemata;
	// insert more if u like
	public String[] schemaFolder = { "DekiWiki", "biosql" };

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		for (int i = 0; i < schemaFolder.length; i++) {
			readSchemaHistory(schemaFolder[i]);
		}

	}

	public void readSchemaHistory(String schemaFolder) throws IOException {
		
		SchemataDifferencesManager schemaManager = new SchemataDifferencesManager();
		schemaManager.getDifferencesInSchemataHistoryAndExport(new File("tests/schemata/" + schemaFolder + "/schemata"));
		
//		DifferencesResult res = new DifferencesResult();
//		res.clear();
//		Transitions trs = new Transitions();
//		File folder = new File("tests/schemata/" + schemaFolder + "/schemata");
//		String[] list = folder.list();
//		String path = folder.getAbsolutePath();
//		java.util.Arrays.sort(list);
//
//		MetricsExport.initMetrics(path);
//		for (int i = 0; i < list.length - 1; i++) {
//			Schema schema = HecateParser.parse(path + File.separator + list[i]);
//			for (Entry<String, Table> e : schema.getTables().entrySet()) {
//				String tname = e.getKey();
//				int attrs = e.getValue().getSize();
//				res.tablesInfo.addTable(tname, i, attrs);
//			}
//			Schema schema2 = HecateParser.parse(path + File.separator + list[i + 1]);
//			if (i == list.length - 2) {
//				for (Entry<String, Table> e : schema2.getTables().entrySet()) {
//					String tname = e.getKey();
//					int attrs = e.getValue().getSize();
//					res.tablesInfo.addTable(tname, i + 1, attrs);
//				}
//			}
//			res = DifferencesAlgorithm.getDifferencesBetweenTwoSchemata(schema, schema2);
//			trs.add(res.myTransformationList);
//			MetricsExport.metrics(res, path);
//		}
//		try {
//			csvExport.tables(path, res.myMetrics.getNumRevisions() + 1, res.tablesInfo);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		xmlExport.xml(trs, path);
//
//		folder = null;
	}

	@Test
	public void test() {
		try {
			String filesToCheck[] = { "all.csv", "metrics.csv", "table_del.csv", "table_ins.csv", "table_key.csv",
					"table_stats.csv", "table_type.csv", "tables.csv", "transitions.xml" };

			for (int i = 0; i < schemaFolder.length; i++) {
				for (int j = 0; j < filesToCheck.length; j++) {

					BufferedReader rightMetricsReader = new BufferedReader(
							new FileReader("tests/schemata/" + schemaFolder[i] + "/rightresults/" + filesToCheck[j]));
					BufferedReader producedMetricsReader = new BufferedReader(
							new FileReader("tests/schemata/" + schemaFolder[i] + "/results/" + filesToCheck[j]));

					String rightLine = "";
					String producedLine = "";
					String cvsSplitBy = ";";
					int line =0;
					while ((rightLine = rightMetricsReader.readLine()) != null) {
						line +=1;
						if ((producedLine = producedMetricsReader.readLine()) == null) {
							assertNull(producedLine);
						}
						// use comma as separator
						String[] rightMetrics = rightLine.split(cvsSplitBy);
						String[] producedMetrics = producedLine.split(cvsSplitBy);
						for (int i1 = 0; i1 < rightMetrics.length; i1++) {

							if (rightMetrics[i1].equals(producedMetrics[i1]) == false)
								fail("Expected :" + rightMetrics[i1] + ", Found :" + producedMetrics[i1] + " in "
										+ schemaFolder[i] +" line : "+line +" of file : "+filesToCheck[j]);

						}
					}
					rightMetricsReader.close();
					producedMetricsReader.close();

				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("FileNotFoundException");

		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
	}

	public void checkSchemaResults(String schemaFolder) throws IOException {
		BufferedReader rightMetricsReader = new BufferedReader(
				new FileReader("tests/schemata/" + schemaFolder + "rightresults/metrics.csv"));
		BufferedReader producedMetricsReader = new BufferedReader(
				new FileReader("tests/schemata/" + schemaFolder + "/results/metrics.csv"));

		String rightLine = "";
		String producedLine = "";
		String cvsSplitBy = ";";
		while ((rightLine = rightMetricsReader.readLine()) != null) {
			if ((producedLine = producedMetricsReader.readLine()) == null) {
				assertNull(producedLine);
			}
			// use comma as separator
			String[] rightMetrics = rightLine.split(cvsSplitBy);
			String[] producedMetrics = producedLine.split(cvsSplitBy);
			for (int i = 0; i < rightMetrics.length; i++) {
				System.out.println(rightMetrics[i]);
				System.out.println(producedMetrics[i]);
				if (rightMetrics[i].equals(producedMetrics[i]) == false)
					fail("Expected :" + rightMetrics[i] + ", Found :" + producedMetrics[i]);

			}
		}

		rightMetricsReader.close();
		producedMetricsReader.close();

	}

}
