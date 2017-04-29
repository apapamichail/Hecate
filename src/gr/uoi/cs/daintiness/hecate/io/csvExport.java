	package gr.uoi.cs.daintiness.hecate.io;

	import java.io.BufferedWriter;
	import java.io.File;
	import java.io.FileWriter;
	import java.io.IOException;

	import javax.swing.JOptionPane;

	import gr.uoi.cs.daintiness.hecate.metrics.tables.Changes;
	import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;
	import gr.uoi.cs.daintiness.hecate.metrics.tables.TablesInfo;
	
	public class csvExport extends Export {

		public static void tablesCSV(String path, int versions, TablesInfo tableinfo) {
			String slashedPath = Export.getDir(path) + File.separator;
			String sTab = slashedPath + "tables.csv";
			String sTabI = slashedPath + "table_ins.csv";
			String sTabD = slashedPath + "table_del.csv";
			String sTabT = slashedPath + "table_type.csv";
			String sTabK = slashedPath + "table_key.csv";
			String sTabAll = slashedPath + "all.csv";
			String sTabSt = slashedPath + "table_stats.csv";

			try {
				BufferedWriter fTab = new BufferedWriter(new FileWriter(sTab));
				BufferedWriter fTabI = new BufferedWriter(new FileWriter(sTabI));
				BufferedWriter fTabD = new BufferedWriter(new FileWriter(sTabD));
				BufferedWriter fTabT = new BufferedWriter(new FileWriter(sTabT));
				BufferedWriter fTabK = new BufferedWriter(new FileWriter(sTabK));
				BufferedWriter fTabAll = new BufferedWriter(new FileWriter(sTabAll));
				BufferedWriter fTabSt = new BufferedWriter(new FileWriter(sTabSt));

				writeVersionsLine(fTab, versions);
				writeVersionsLine(fTabI, versions);
				writeVersionsLine(fTabD, versions);
				writeVersionsLine(fTabT, versions);
				writeVersionsLine(fTabK, versions);
				writeVersionsLine(fTabAll, versions);
				fTabSt.write("table;dur;birth;death;chngs;s@s;s@e;sAvg\n");

				for (String t : tableinfo.getTables()){
					fTab.write(t + ";");
					fTabI.write(t + ";");
					fTabD.write(t + ";");
					fTabT.write(t + ";");
					fTabK.write(t + ";");
					fTabAll.write(t + ";");

					fTabSt.write(t + ";");
					MetricsOverVersion mov = tableinfo.getTableMetrics(t);
					fTabSt.write(mov.getLife() + ";");
					fTabSt.write(mov.getBirth() + ";");

					fTabSt.write((mov.getDeath()==versions-1 ? "-" : mov.getDeath())
							+ ";");

					fTabSt.write(mov.getTotalChanges().getTotal() + ";");
					fTabSt.write(mov.getBirthSize() + ";");
					fTabSt.write(mov.getDeathSize() + ";");

					int sumSize = 0;
					int v = 0;
					for (int i = 0; i < versions; i++) {
						if (mov != null && mov.containsKey(i)) {
							fTab.write(mov.getSize(i) + ";");
							Changes c = mov.getChanges(i);
							fTabI.write(c.getInsertions() + ";");
							fTabD.write(c.getDeletions() + ";");
							fTabT.write(c.getAttrTypeChange() + ";");
							fTabK.write(c.getKeyChange() + ";");
							fTabAll.write(mov.getSize(i) + "[" + c.toString() +
									"]" + ";");
							sumSize += mov.getSize(i);
							v++;

						} else {
							fTab.write("0;");
							fTabI.write("-;");
							fTabD.write("-;");
							fTabT.write("-;");
							fTabK.write("-;");
							fTabAll.write("0|-|-|-|-;");
						}
					}
					fTabSt.write(Float.toString((sumSize/(float)v)));
					fTabSt.write("\n");

					fTab.write("\n");
					fTabI.write("\n");
					fTabD.write("\n");
					fTabT.write("\n");
					fTabK.write("\n");
					fTabAll.write("\n");
				}
				fTab.close();
				fTabI.close();
				fTabD.close();
				fTabT.close();
				fTabK.close();
				fTabAll.close();
				fTabSt.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Oups...",
						                      JOptionPane.ERROR_MESSAGE);
			}
		}
		private static void writeVersionsLine(BufferedWriter file, int versions)
				throws IOException {
			file.write(";");
			for (int i = 0; i < versions; i++) {
				file.write(i + ";");
			}
			file.write("\n");
		}
	}


