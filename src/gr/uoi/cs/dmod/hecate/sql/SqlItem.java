package gr.uoi.cs.dmod.hecate.sql;

public interface SqlItem {
	
	public String print();
	
	public void setMode(char c);
	
	public String getName();
	
	public char getMode();
	
	public String whatAmI();
}
