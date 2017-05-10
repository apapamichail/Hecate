ckage gr.uoi.cs.daintiness.hecate.exportinformation;

import gr.uoi.cs.daintiness.hecate.differencedetection.DifferencesResult;
import gr.uoi.cs.daintiness.hecate.metrics.tables.TablesInfo;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

public interface ExportInterface {
	public static void tablesCSV(String path, int versions, TablesInfo tableinfo){}
	public static void metrics(DifferencesResult result, String path){}
	public static void xml(Transitions transition, String path) {}
}
