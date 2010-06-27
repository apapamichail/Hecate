package gr.uoi.cs.dmod.hecate.sql;

import java.util.Map;
import java.util.TreeMap;

public class Schema {
	String title;
	TreeMap<String, Table> tables;
	
	public Schema(TreeMap<String, Table> t) {
		this.tables = t;
	}
	
	public TreeMap<String, Table> getTables() {
		return this.tables;
	}
	
	public String toString() {
		String buff = new String();
		buff = "Shema: \n\n";
		for (Map.Entry<String, Table> entry : this.tables.entrySet()) {
			Table a = entry.getValue();
			buff += "  " + a.toString() + "\n";
		}
		return buff;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}
}