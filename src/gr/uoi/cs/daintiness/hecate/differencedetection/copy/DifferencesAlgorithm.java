package gr.uoi.cs.daintiness.hecate.differencedetection.copy;

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
public class DifferencesAlgorithm {

  	
	//refactoring customizations the idea is to put them global so we can refactor
	private String oldTableKey = null, newTableKey = null ;
	private String oldAttrKey = null, newAttrKey = null ;
	private SchemataDifferencesHelper differencesHelper;
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
	 * @param schemaA
	 *   The original schema
	 * @param schemaB 
	 * @param schemaB
	 *   The modified version of the original schema
	 */
	
	public DifferencesResult getDifferencesBetweenTwoSchemata(Schema schemaA, Schema schemaB) {
		differencesHelper = new SchemataDifferencesHelper();

		differencesHelper.setUp(schemaA, schemaB);
		
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

	/**
	 * @param oldTable
	 * @param newTable
	 */
	private  void findSameTablesDifferences(Table oldTable, Table newTable) {
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
	}
