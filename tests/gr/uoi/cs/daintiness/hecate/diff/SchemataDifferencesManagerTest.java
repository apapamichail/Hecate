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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import com.sun.media.jfxmedia.logging.Logger;

import gr.uoi.cs.daintiness.hecate.differencedetection.DifferencesResult;
import gr.uoi.cs.daintiness.hecate.differencedetection.SchemataDifferencesManager;
import gr.uoi.cs.daintiness.hecate.io.*;
import gr.uoi.cs.daintiness.hecate.io.MetricsExport;
import gr.uoi.cs.daintiness.hecate.parser.HecateParser;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

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
		DifferencesResult res = new DifferencesResult();
		res.clear();
		Transitions trs = new Transitions();
		File folder = new File("tests/schemata/" + schemaFolder + "/schemata");
		String[] list = folder.list();
		String path = folder.getAbsolutePath();
		java.util.Arrays.sort(list);

		MetricsExport.initMetrics(path);
		for (int i = 0; i < list.length - 1; i++) {
			Schema schema = HecateParser.parse(path + File.separator + list[i]);
			for (Entry<String, Table> e : schema.getTables().entrySet()) {
				String tname = e.getKey();
				int attrs = e.getValue().getSize();
				res.tablesInfo.addTable(tname, i, attrs);
			}
			Schema schema2 = HecateParser.parse(path + File.separator + list[i + 1]);
			if (i == list.length - 2) {
				for (Entry<String, Table> e : schema2.getTables().entrySet()) {
					String tname = e.getKey();
					int attrs = e.getValue().getSize();
					res.tablesInfo.addTable(tname, i + 1, attrs);
				}
			}
			res = SchemataDifferencesManager.getDifferencesBetweenTwoSchemata(schema, schema2);
			trs.add(res.myTransformationList);
			MetricsExport.metrics(res, path);
		}
		try {
			csvExport.tablesCSV(path, res.myMetrics.getNumRevisions() + 1, res.tablesInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xmlExport.xml(trs, path);

		folder = null;

	}

	@Test
	public void test() {
		try {
			String csvFiles[] = { "all.csv", "metrics.csv", "table_del.csv", "table_ins.csv", "table_key.csv",
					"table_stats.csv", "table_type.csv", "tables.csv" };

			for (int i = 0; i < schemaFolder.length; i++) {
				for (int j = 0; j < csvFiles.length; j++) {

					BufferedReader rightMetricsReader = new BufferedReader(
							new FileReader("tests/schemata/" + schemaFolder[i] + "/rightresults/" + csvFiles[j]));
					BufferedReader producedMetricsReader = new BufferedReader(
							new FileReader("tests/schemata/" + schemaFolder[i] + "/results/" + csvFiles[j]));

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
						for (int i1 = 0; i1 < rightMetrics.length; i1++) {

							if (rightMetrics[i1].equals(producedMetrics[i1]) == false)
								fail("Expected :" + rightMetrics[i1] + ", Found :" + producedMetrics[i1] + " in "
										+ schemaFolder[i]);

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
