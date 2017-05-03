package gr.uoi.cs.daintiness.hecate.differencedetection;

import gr.uoi.cs.daintiness.hecate.metrics.tables.ChangeType;
import gr.uoi.cs.daintiness.hecate.sql.Attribute;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.SqlItem;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Deletion;
import gr.uoi.cs.daintiness.hecate.transitions.Insersion;
import gr.uoi.cs.daintiness.hecate.transitions.Update;

import java.util.Iterator;

/**
 * This class is responsible for performing the diff algorithm
 * between two SQL schemas. It then stores some metrics about the
 * performed diff.
 * @author giskou
 *
 */
public class DifferencesAlgorithmSkoulis implements Algorithm  {

  	
	//refactoring customizations the idea is to put them global so we can refactor
	private String oldTableKey = null, newTableKey = null ;
	private String oldAttrKey = null, newAttrKey = null ;
	private SchemataDifferencesHelper differencesHelper;

	public  Insersion in;
	public  Deletion out;
	public  Update up;
	public  Iterator<String> oldAttributeKeys;
	public  Iterator<Attribute> oldAttributeValues;
	public  Iterator<String> newAttributeKeys;
	public  Iterator<Attribute> newAttributeValues;
	
	public  Iterator<String> oldTableKeys;
	public  Iterator<Table> oldTableValues;
	public  Iterator<String> newTableKeys;
	public  Iterator<Table> newTableValues;
	public  DifferencesResult results;

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#getDifferencesBetweenTwoSchemata(gr.uoi.cs.daintiness.hecate.sql.Schema, gr.uoi.cs.daintiness.hecate.sql.Schema)
	 */
	
	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#getDifferencesBetweenTwoSchemata(gr.uoi.cs.daintiness.hecate.sql.Schema, gr.uoi.cs.daintiness.hecate.sql.Schema)
	 */
	@Override
	public DifferencesResult getDifferencesBetweenTwoSchemata(Schema schemaA, Schema schemaB) {
		

		setUp(schemaA, schemaB);
		
		if (oldTableKeys.hasNext() && newTableKeys.hasNext()){
			oldTableKey = oldTableKeys.next() ;
			Table oldTable = (Table) oldTableValues.next() ;
			newTableKey = newTableKeys.next() ;
			Table newTable = (Table) newTableValues.next() ;
			while(true) {
				in = null; out = null; up = null;
				if (oldTableKey.compareTo(newTableKey) == 0) {            // ** Matched tables
					match(oldTable, newTable);

					findSameTablesDifferences(oldTable, newTable);
					
					if (oldTableKeys.hasNext() && newTableKeys.hasNext()) {   // move both tables
						oldTableKey = oldTableKeys.next() ;
						oldTable = (Table) oldTableValues.next() ;
						newTableKey = newTableKeys.next() ;
						newTable = (Table) newTableValues.next() ;
						continue;
					} else {            // one list is empty
						break ;
					}
				} else if (oldTableKey.compareTo(newTableKey) < 0) {  // ** Table Deleted
					deleteTable(oldTable);
					if (oldTableKeys.hasNext()) {                     // move old only
						oldTableKey = oldTableKeys.next() ;
						oldTable = (Table) oldTableValues.next() ;
						continue;
					} else {
						insertTable(newTable);
						break;
					}
				} else {                                             // ** Table Inserted
					insertTable(newTable);
					if (newTableKeys.hasNext()) {                    // move new only
						newTableKey = newTableKeys.next() ;
						newTable = (Table) newTableValues.next() ;
						continue;
					} else {
						deleteTable(oldTable);
						break ;
					}
				}
			}
		}

		checkRemainingTableKeysforOld();
		checkRemainingTableKeysforNew();
		
		// no need for it : results.myMetrics.sanityCheck();
		
		return results;
	}

	@Override
	/**
	 * @param oldTable
	 * @param newTable
	 */
	public void findSameTablesDifferences(Table oldTable, Table newTable) {
		initializeAttributesKeys(oldTable, newTable);

		initializeAttributesValues(oldTable, newTable);

		computeAttributesDifferences(oldTable, newTable);
		// check remaining attributes
		while (oldAttributeKeys.hasNext()) {       // delete remaining old (not found in new)
			deleteAttributesNotInNew(newTable);
		}
		while (newAttributeKeys.hasNext()) {        // insert remaining new (not found in old)
			insertAttributesNotInOld(oldTable);
		}
		//  ** Done with attributes **
		if (newTable.getMode() == SqlItem.UPDATED) {
			alterTable(newTable);
		}
	}
	/**
	 * @param oldTable
	 * @param newTable
	 */
	private  void computeAttributesDifferences(Table oldTable, Table newTable) {
		String oldAttrKey;
		String newAttrKey;
		if (oldAttributeKeys.hasNext() && newAttributeKeys.hasNext()){
			oldAttrKey = oldAttributeKeys.next() ;
			Attribute oldAttr = oldAttributeValues.next();
			newAttrKey = newAttributeKeys.next() ;
			Attribute newAttr = newAttributeValues.next();
 			while (true) {
    				
 				if (oldAttrKey.compareTo(newAttrKey) == 0) {                   // possible attribute match
					if (oldAttr.getType().compareTo(newAttr.getType()) == 0){  // check attribute type
						if (oldAttr.isKey() == newAttr.isKey()) {              // ** Matched attributes
							match(oldAttr, newAttr);
						} else {                                               // * attribute key changed
							attributeKeyChange(oldAttr, newAttr);
						}
					} else {                                                   // attribute type changed
						attributeTypeChange(oldAttr, newAttr);
					}
					// move both attributes
					if (oldAttributeKeys.hasNext() && newAttributeKeys.hasNext()) {
						oldAttrKey = oldAttributeKeys.next() ;
						oldAttr = oldAttributeValues.next();
						newAttrKey = newAttributeKeys.next() ;
						newAttr = newAttributeValues.next();
						continue;
					} else {            // one of the lists is empty, must process the rest of the other
						break ;
					}
				} else if (oldAttrKey.compareTo(newAttrKey) < 0) {           // ** Deleted attributes
					attributeDelete(oldAttr, newTable);
					// move old only attributes
					if (oldAttributeKeys.hasNext()) {
						oldAttrKey = oldAttributeKeys.next();
						oldAttr = oldAttributeValues.next();
						continue;
					} else {                  // no more old
						attributeInsert(oldTable, newAttr);
						break ;
					}
				} else {                    // ** Inserted attributes
					attributeInsert(oldTable, newAttr);
					// move new only
					if (newAttributeKeys.hasNext()) {
						newAttrKey = newAttributeKeys.next() ;
						newAttr = newAttributeValues.next();
						continue;
					} else {                  // no more new
						attributeDelete(oldAttr, newTable);
						break ;
					}
				}
				
			}
			
		}
	}
	
	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#setUp(gr.uoi.cs.daintiness.hecate.sql.Schema, gr.uoi.cs.daintiness.hecate.sql.Schema)
	 */
	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#setUp(gr.uoi.cs.daintiness.hecate.sql.Schema, gr.uoi.cs.daintiness.hecate.sql.Schema)
	 */
	@Override
	public  void setUp(Schema schemaA, Schema schemaB) {
		results = new DifferencesResult();
		
		results.myMetrics.newRevision();
		results.setVersionNames(schemaA.getName(), schemaB.getName());
		oldTableKeys = schemaA.getTables().keySet().iterator();
		oldTableValues = schemaA.getTables().values().iterator();
		newTableKeys = schemaB.getTables().keySet().iterator();
		newTableValues = schemaB.getTables().values().iterator();
		setOriginalSizes(schemaA.getSize(), schemaB.getSize());

	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#checkRemainingTableKeysforNew()
	 */
	@Override
	public  void checkRemainingTableKeysforNew() {
		String newTableKey;
		while (newTableKeys.hasNext()) {
			newTableKey = (String) newTableKeys.next();
			Table newTable = (Table) newTableValues.next();
			insertTable(newTable);
		}
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#checkRemainingTableKeysforOld()
	 */
	@Override
	public  void checkRemainingTableKeysforOld() {
		String oldTableKey;
		while (oldTableKeys.hasNext()) {
			oldTableKey = (String) oldTableKeys.next();
			Table oldTable = (Table) oldTableValues.next();
			deleteTable(oldTable);
		}
	}
	
	


	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#insertAttributesNotInOld(gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	public  void insertAttributesNotInOld(Table oldTable) {
		String newAttrKey;
		newAttrKey = (String) newAttributeKeys.next();
		Attribute newAttr = newAttributeValues.next();
		attributeInsert(oldTable, newAttr);
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#deleteAttributesNotInNew(gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	public  void deleteAttributesNotInNew(Table newTable) {
		String oldAttrKey;
		oldAttrKey = (String) oldAttributeKeys.next();
		Attribute oldAttr = oldAttributeValues.next();
		attributeDelete(oldAttr, newTable);
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#initializeAttributesValues(gr.uoi.cs.daintiness.hecate.sql.Table, gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	public  void initializeAttributesValues(Table oldTable, Table newTable) {
		oldAttributeValues = oldTable.getAttrs().values().iterator();
		newAttributeValues = newTable.getAttrs().values().iterator();
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#initializeAttributesKeys(gr.uoi.cs.daintiness.hecate.sql.Table, gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	public  void initializeAttributesKeys(Table oldTable, Table newTable) {
		oldAttributeKeys = oldTable.getAttrs().keySet().iterator();
		newAttributeKeys = newTable.getAttrs().keySet().iterator();
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#attributeInsert(gr.uoi.cs.daintiness.hecate.sql.Table, gr.uoi.cs.daintiness.hecate.sql.Attribute)
	 */
	public  void attributeInsert(Table oldTable, Attribute newAttr) {
		results.myMetrics.insertAttr();
		insertItemInList(newAttr);
		newAttr.setMode(SqlItem.INSERTED);
		oldTable.setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(oldTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Insertion);
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#attributeDelete(gr.uoi.cs.daintiness.hecate.sql.Attribute, gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	public  void attributeDelete(Attribute oldAttr, Table newTable) {
		results.myMetrics.deleteAttr();
		deleteItem(oldAttr);
		oldAttr.setMode(SqlItem.DELETED);
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newTable.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Deletion);
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#attributeTypeChange(gr.uoi.cs.daintiness.hecate.sql.Attribute, gr.uoi.cs.daintiness.hecate.sql.Attribute)
	 */
	public  void attributeTypeChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterAttr();
		updateAttribute(newAttr, "TypeChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(),
				ChangeType.AttrTypeChange);
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#attributeKeyChange(gr.uoi.cs.daintiness.hecate.sql.Attribute, gr.uoi.cs.daintiness.hecate.sql.Attribute)
	 */
	public  void attributeKeyChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterKey();
		updateAttribute(newAttr, "KeyChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(),
				ChangeType.KeyChange);
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#deleteTable(gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	@Override
	public  void deleteTable(Table t) {
		deleteItem(t);
		results.myMetrics.deleteTable();
		markAll(t, SqlItem.DELETED); // mark attributes deleted
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#insertTable(gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	@Override
	public  void insertTable(Table t) {
		insertItemInList(t);
		results.myMetrics.insetTable();
		markAll(t, SqlItem.INSERTED); // mark attributes inserted
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#alterTable(gr.uoi.cs.daintiness.hecate.sql.Table)
	 */
	@Override
	public  void alterTable(Table t) {
		results.myMetrics.alterTable();
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#match(gr.uoi.cs.daintiness.hecate.sql.SqlItem, gr.uoi.cs.daintiness.hecate.sql.SqlItem)
	 */
	@Override
	public  void match(SqlItem oldI, SqlItem newI) {
		oldI.setMode(SqlItem.MACHED);
		newI.setMode(SqlItem.MACHED);
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#markAll(gr.uoi.cs.daintiness.hecate.sql.Table, int)
	 */
	public  void markAll(Table t, int mode) {
		t.setMode(mode);
		for (Iterator<Attribute> i = t.getAttrs().values().iterator(); i.hasNext();) {
			i.next().setMode(mode);
			switch (mode) {
			case SqlItem.INSERTED:
				results.myMetrics.insertTabAttr();
				break;
			case SqlItem.DELETED:
				results.myMetrics.deleteTabAttr();
				break;
			default:
				;
			}
		}
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#insertItemInList(gr.uoi.cs.daintiness.hecate.sql.SqlItem)
	 */
	public  void insertItemInList(SqlItem item) {
		if (item.getClass() == Attribute.class) {
			if (in == null) {
				in = new Insersion();
				results.myTransformationList.add(in);
			}
			try {
				in.attribute((Attribute) item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (item.getClass() == Table.class) {
			in = new Insersion();
			results.myTransformationList.add(in);
			in.table((Table) item);
		}
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#deleteItem(gr.uoi.cs.daintiness.hecate.sql.SqlItem)
	 */
	public  void deleteItem(SqlItem item) {
		if (item.getClass() == Attribute.class) {
			if (out == null) {
				out = new Deletion();
				results.myTransformationList.add(out);
			}
			try {
				out.attribute((Attribute) item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (item.getClass() == Table.class) {
			out = new Deletion();
			results.myTransformationList.add(out);
			out.table((Table) item);
		}
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#updateAttribute(gr.uoi.cs.daintiness.hecate.sql.Attribute, java.lang.String)
	 */
	public  void updateAttribute(Attribute item, String type) {
		if (up == null) {
			up = new Update();
			results.myTransformationList.add(up);
		}
		try {
			up.updateAttribute((Attribute) item, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see gr.uoi.cs.daintiness.hecate.differencedetection.Algorithm#setOriginalSizes(int[], int[])
	 */
	@Override
	public  void setOriginalSizes(int[] sizeA, int[] sizeB) {
		results.myMetrics.setOrigTables(sizeA[0]);
		results.myMetrics.setOrigAttrs(sizeA[1]);
		results.myMetrics.setNewTables(sizeB[0]);
		results.myMetrics.setNewAttrs(sizeB[1]);
	}
	}
