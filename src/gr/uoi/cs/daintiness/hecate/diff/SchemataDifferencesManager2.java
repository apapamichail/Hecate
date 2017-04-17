package gr.uoi.cs.daintiness.hecate.diff;

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
public class SchemataDifferencesManager2 {

	private static Insersion in;
	private static Deletion out;
	private static Update up;
	private static DifferencesResult results;
	
	//refactoring customizations the idea is to put them global so we can refactor
	private static Iterator<String> oldAttributeKeys ;
	private static Iterator<Attribute> oldAttributeValues ;
	private static Iterator<String> newAttributeKeys;
	private static Iterator<Attribute> newAttributeValues;
	private static Iterator<String> oldTableKeys;
	private static Iterator<Table> oldTableValues;
	private static Iterator<String> newTableKeys;
	private static Iterator<Table> newTableValues;
	/**
	 * This function performs the main diff algorithm for
	 * finding the differences between the schemas that are 
	 * given as parameters. The algorithm is a modification of
	 * the SortMergeJoin algorithm found at DBMS's for joining
	 * two tables. The tables and attributes are stored on TreeMaps
	 * thus sorted according their name. Starting from the top of
	 * each Map we check the items for matches. If the original is
	 * larger lexicographically then the item of the modified Map does
	 * not exist in the original and so it's inserted and we move to
	 * the next item on the modified Map. Likewise, if the modified
	 * is larger lexicographically then the item on the original has been
	 * deleted and we move to the next item on the original Map. If a
	 * Map reaches at an end then the remaining items on the other Map
	 * are marked as inserted or deleted accordingly.
	 * @param schemaA
	 *   The original schema
	 * @param schemaB
	 *   The modified version of the original schema
	 */
	public static DifferencesResult getDifferencesBetweenTwoSchemata(Schema schemaA, Schema schemaB) {
		results = new DifferencesResult();
		results.myMetrics.newRevision();
		results.setVersionNames(schemaA.getName(), schemaB.getName());
		
		String oldTableKey = null, newTableKey = null ;
		String oldAttrKey = null, newAttrKey = null ;
		oldTableKeys = schemaA.getTables().keySet().iterator() ;
		oldTableValues = schemaA.getTables().values().iterator() ;
		newTableKeys = schemaB.getTables().keySet().iterator() ;
		newTableValues = schemaB.getTables().values().iterator() ;
		setOriginalSizes(schemaA.getSize(), schemaB.getSize());
		
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
		
		results.myMetrics.sanityCheck();//Test need to be MOVEED!!!!!!!!!!!!!
		
		return results;
	}

	/**
	 * @param oldTable
	 * @param newTable
	 */
	private static void findSameTablesDifferences(Table oldTable, Table newTable) {
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
	 * 
	 */
	private static void checkRemainingTableKeysforNew() {
		String newTableKey;
		while (newTableKeys.hasNext()) {
			newTableKey = (String) newTableKeys.next();
			Table newTable = (Table) newTableValues.next();
			insertTable(newTable);
		}
	}

	/**
	 * 
	 */
	private static void checkRemainingTableKeysforOld() {
		String oldTableKey;
		while (oldTableKeys.hasNext()) {
			oldTableKey = (String) oldTableKeys.next();
			Table oldTable = (Table) oldTableValues.next();
			deleteTable(oldTable);
		}
	}

	/**
	 * @param oldTable
	 */
	private static void insertAttributesNotInOld(Table oldTable) {
		String newAttrKey;
		newAttrKey = (String) newAttributeKeys.next();
		Attribute newAttr = newAttributeValues.next();
		attributeInsert(oldTable, newAttr);
	}

	/**
	 * @param newTable
	 */
	private static void deleteAttributesNotInNew(Table newTable) {
		String oldAttrKey;
		oldAttrKey = (String) oldAttributeKeys.next();
		Attribute oldAttr = oldAttributeValues.next();
		attributeDelete(oldAttr, newTable);
	}

	/**
	 * @param oldTable
	 * @param newTable
	 */
	private static void initializeAttributesValues(Table oldTable, Table newTable) {
		oldAttributeValues = oldTable.getAttrs().values().iterator() ;
		newAttributeValues = newTable.getAttrs().values().iterator() ;
	}

	/**
	 * @param oldTable
	 * @param newTable
	 */
	private static void initializeAttributesKeys(Table oldTable, Table newTable) {
		oldAttributeKeys = oldTable.getAttrs().keySet().iterator();
		newAttributeKeys = newTable.getAttrs().keySet().iterator();
	}

	/**
	 * @param oldTable
	 * @param newTable
	 */
	private static void computeAttributesDifferences(Table oldTable, Table newTable) {
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

	private static void attributeInsert(Table oldTable, Attribute newAttr) {
		results.myMetrics.insertAttr();
		insertItemInList(newAttr);
		newAttr.setMode(SqlItem.INSERTED);
		oldTable.setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(oldTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Insertion);
	}

	private static void attributeDelete(Attribute oldAttr, Table newTable) {
		results.myMetrics.deleteAttr();
		deleteItem(oldAttr);
		oldAttr.setMode(SqlItem.DELETED);
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newTable.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Deletion);
	}

	private static void attributeTypeChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterAttr();
		updateAttribute(newAttr, "TypeChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(), ChangeType.AttrTypeChange);
	}

	private static void attributeKeyChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterKey();
		updateAttribute(newAttr, "KeyChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(), ChangeType.KeyChange);
	}
	
	private static void deleteTable(Table t) {
		deleteItem(t);
		results.myMetrics.deleteTable();
		markAll(t, SqlItem.DELETED);     // mark attributes deleted
	}
	
	private static void insertTable(Table t) {
		insertItemInList(t);
		results.myMetrics.insetTable();
		markAll(t, SqlItem.INSERTED);     // mark attributes inserted
	}
	
	private static void alterTable(Table t) {
		results.myMetrics.alterTable();
	}

	private static void match(SqlItem oldI, SqlItem newI) {
		oldI.setMode(SqlItem.MACHED);
		newI.setMode(SqlItem.MACHED);
	}

	private static void markAll(Table t, int mode) {
		t.setMode(mode);
		for (Iterator<Attribute> i = t.getAttrs().values().iterator(); i.hasNext(); ) {
			i.next().setMode(mode);
			switch(mode){
				case SqlItem.INSERTED: results.myMetrics.insertTabAttr(); break;
				case SqlItem.DELETED: results.myMetrics.deleteTabAttr(); break;
				default:;
			}
		}
	}
	
	private static void insertItemInList(SqlItem item) {
		if (item.getClass() == Attribute.class) {
			if (in == null) {
				in = new Insersion();
				results.myTransformationList.add(in);
			}
			try {
				in.attribute( (Attribute) item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (item.getClass() == Table.class) {
			in = new Insersion();
			results.myTransformationList.add(in);
			in.table( (Table) item);
		}
	}
	
	private static void deleteItem(SqlItem item) {
		if (item.getClass() == Attribute.class) {
			if (out == null) {
				out = new Deletion();
				results.myTransformationList.add(out);
			}
			try {
				out.attribute( (Attribute) item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (item.getClass() == Table.class) {
			out = new Deletion();
			results.myTransformationList.add(out);
			out.table( (Table) item);
		}
	}
	
	private static void updateAttribute(Attribute item, String type) {
		if (up == null) {
			up = new Update();
			results.myTransformationList.add(up);
		}
		try {
			up.updateAttribute((Attribute)item, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setOriginalSizes(int[] sizeA, int[] sizeB) {
		results.myMetrics.setOrigTables(sizeA[0]); results.myMetrics.setOrigAttrs(sizeA[1]);
		results.myMetrics.setNewTables(sizeB[0]); results.myMetrics.setNewAttrs(sizeB[1]);
	}
}
