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
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.sun.media.jfxmedia.logging.Logger;

import gr.uoi.cs.daintiness.hecate.io.Export;
import gr.uoi.cs.daintiness.hecate.parser.HecateParser;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

/**
 * @author angelo
 *
 */
public class SchemataDifferencesManagerTest {
	File schemata;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		DifferencesResult res = new DifferencesResult();
		res.clear();
		Transitions trs = new Transitions();
		File folder = new File("tests/schemata/Zabbix/mysql");
		String[] list = folder.list();
 		String path = folder.getAbsolutePath();
		java.util.Arrays.sort(list);
		
		Export.initMetrics(path);
		for (int i = 0; i < list.length-1; i++) {
 			Schema schema = HecateParser.parse(path + File.separator + list[i]);
			for (Entry<String, Table> e : schema.getTables().entrySet()) {
				String tname = e.getKey();
				int attrs = e.getValue().getSize();
				res.tablesInfo.addTable(tname, i, attrs);
			}
 			Schema schema2 = HecateParser.parse(path + File.separator + list[i+1]);
			if (i == list.length-2) {
				for (Entry<String, Table> e : schema2.getTables().entrySet()) {
					String tname = e.getKey();
					int attrs = e.getValue().getSize();
					res.tablesInfo.addTable(tname, i+1, attrs);
				}
			}
 			res = SchemataDifferencesManager.getDifferencesBetweenTwoSchemata(schema, schema2);
			trs.add(res.myTransformationList);
			Export.metrics(res, path);
 		}
		try {
			Export.tables(path, res.myMetrics.getNumRevisions()+1, res.tablesInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Export.xml(trs, path);
		
		folder = null;
		}

	@Test
	public void test() {
		 String rightLine = "";
	        String producedLine = "";
	        String cvsSplitBy = ";";
	        try {

	        	BufferedReader rightMetricsReader  = new BufferedReader(new FileReader("tests/schemata/Zabbix/rightMetrics.csv"));
	     		BufferedReader producedMetricsReader  = new BufferedReader(new FileReader("tests/schemata/Zabbix/results/metrics.csv"));

	            while ((rightLine = rightMetricsReader .readLine()) != null) {
	            	if((producedLine = producedMetricsReader .readLine()) == null){
	            		assertNull(producedLine);
	            	}
	                // use comma as separator
	                String[] rightMetrics = rightLine.split(cvsSplitBy);
	                String[] producedMetrics = producedLine.split(cvsSplitBy);
	                for (int i=0; i < rightMetrics.length; i++){
	                	System.out.println(rightMetrics[i] );
	                	System.out.println(producedMetrics[i] );
	                	if(rightMetrics[i].equals(producedMetrics[i]) == false)
	                		fail("Expected :"+rightMetrics[i]+", Found :"+producedMetrics[i]);
	                		
	                	}
	                }

	            

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        
	        }
	}
 
}
