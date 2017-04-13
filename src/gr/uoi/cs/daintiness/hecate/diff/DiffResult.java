/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.diff;

import gr.uoi.cs.daintiness.hecate.metrics.Metrics;
import gr.uoi.cs.daintiness.hecate.metrics.tables.TablesInfo;
import gr.uoi.cs.daintiness.hecate.transitions.TransitionList;

/**
 * @author iskoulis
 *
 */
public class DiffResult {

	final public TransitionList myTransformationList;
	final public Metrics myMetrics;
	final public TablesInfo tablesInfo;
	/**
	 * 
	 */
	public DiffResult() {
		this.myTransformationList = new TransitionList();
		this.myMetrics = new Metrics();
		this.tablesInfo = new TablesInfo();
	}
	
	public void setVersionNames(String oldVersion, String newVersion) {
		this.myTransformationList.setVersionNames(oldVersion, newVersion);
		this.myMetrics.setVersionNames(oldVersion, newVersion);
	}
	
	public void clear() {
		this.tablesInfo.clear();
		myMetrics.resetRevisions();
	}
}
