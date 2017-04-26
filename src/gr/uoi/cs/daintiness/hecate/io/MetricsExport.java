package gr.uoi.cs.daintiness.hecate.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gr.uoi.cs.daintiness.hecate.diff.DiffResult;

public class MetricsExport {
	
	public static void metrics(DiffResult result, String path)
			throws IOException {
		String filePath = Export.getDir(path) + File.separator + "metrics.csv";
		FileWriter filewriter = new FileWriter(filePath, true);
		BufferedWriter metrics = new BufferedWriter(filewriter);
		String name = result.met.getVersionNames()[1];
		String time = name.substring(0, name.length()-4);
		metrics.write(
				result.met.getNumRevisions() + ";" +			//trID
				time + ";" +								//time
				result.met.getVersionNames()[0] + ";" +		//oldVer
				result.met.getVersionNames()[1] + ";" +		//newVer
				result.met.getOldSizes()[0] + ";" +			//#oldT
				result.met.getNewSizes()[0] + ";" +			//#newT
				result.met.getOldSizes()[1] + ";" +			//#oldA
				result.met.getNewSizes()[1] + ";" +			//#newA
				result.met.getTableMetrics()[0] + ";" +		//tIns
				result.met.getTableMetrics()[1] + ";" +		//tDel
				result.met.getAttributeMetrics()[0] + ";" +	//aIns
				result.met.getAttributeMetrics()[1] + ";" +	//aDel
				result.met.getAttributeMetrics()[2] + ";" +	//aTypeAlt
				result.met.getAttributeMetrics()[3] + ";" +	//keyAlt
				result.met.getAttributeMetrics()[4] + ";" +	//aTabIns
				result.met.getAttributeMetrics()[5] + "\n"		//aTabDel
			);
		metrics.close();
	}
	public static void initMetrics(String path) throws IOException {
		String filePath = Export.getDir(path) + File.separator + "metrics.csv";
		BufferedWriter metrics = new BufferedWriter(new FileWriter(filePath));
		metrics.write("trID;time;oldVer;newVer;#oldT;#newT;#oldA;#newA"
				+ ";tIns;tDel;aIns;aDel;aTypeAlt;keyAlt;aTabIns;aTabDel\n");
		metrics.close();
	}
}
