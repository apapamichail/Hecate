package gr.uoi.cs.daintiness.hecate.differencedetection.copy;

import java.util.Iterator;

import gr.uoi.cs.daintiness.hecate.metrics.tables.ChangeType;
import gr.uoi.cs.daintiness.hecate.metrics.tables.TablesInfo;
import gr.uoi.cs.daintiness.hecate.sql.Attribute;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.SqlItem;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Deletion;
import gr.uoi.cs.daintiness.hecate.transitions.Insersion;
import gr.uoi.cs.daintiness.hecate.transitions.Update;

public class SchemataDifferencesHelper {

	public static Insersion in;
	public static Deletion out;
	public static Update up;
	public static Iterator<String> oldAttributeKeys;
	public static Iterator<Attribute> oldAttributeValues;
	public static Iterator<String> newAttributeKeys;
	public static Iterator<Attribute> newAttributeValues;
	
	public static Iterator<String> oldTableKeys;
	public static Iterator<Table> oldTableValues;
	public static Iterator<String> newTableKeys;
	public static Iterator<Table> newTableValues;
	public static DifferencesResult results;

	/**
	 * 
	 */
	
	public static void setUp(Schema schemaA, Schema schemaB) {
		results = new DifferencesResult();
		
		results.myMetrics.newRevision();
		results.setVersionNames(schemaA.getName(), schemaB.getName());
		oldTableKeys = schemaA.getTables().keySet().iterator();
		oldTableValues = schemaA.getTables().values().iterator();
		newTableKeys = schemaB.getTables().keySet().iterator();
		newTableValues = schemaB.getTables().values().iterator();
		setOriginalSizes(schemaA.getSize(), schemaB.getSize());

	}

	public static void checkRemainingTableKeysforNew() {
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
	public static void checkRemainingTableKeysforOld() {
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
	public static void insertAttributesNotInOld(Table oldTable) {
		String newAttrKey;
		newAttrKey = (String) newAttributeKeys.next();
		Attribute newAttr = newAttributeValues.next();
		attributeInsert(oldTable, newAttr);
	}

	/**
	 * @param newTable
	 */
	public static void deleteAttributesNotInNew(Table newTable) {
		String oldAttrKey;
		oldAttrKey = (String) oldAttributeKeys.next();
		Attribute oldAttr = oldAttributeValues.next();
		attributeDelete(oldAttr, newTable);
	}

	/**
	 * @param oldTable
	 * @param newTable
	 */
	public static void initializeAttributesValues(Table oldTable, Table newTable) {
		oldAttributeValues = oldTable.getAttrs().values().iterator();
		newAttributeValues = newTable.getAttrs().values().iterator();
	}

	/**
	 * @param oldTable
	 * @param newTable
	 */
	public static void initializeAttributesKeys(Table oldTable, Table newTable) {
		oldAttributeKeys = oldTable.getAttrs().keySet().iterator();
		newAttributeKeys = newTable.getAttrs().keySet().iterator();
	}

	public static void attributeInsert(Table oldTable, Attribute newAttr) {
		results.myMetrics.insertAttr();
		insertItemInList(newAttr);
		newAttr.setMode(SqlItem.INSERTED);
		oldTable.setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(oldTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Insertion);
	}

	public static void attributeDelete(Attribute oldAttr, Table newTable) {
		results.myMetrics.deleteAttr();
		deleteItem(oldAttr);
		oldAttr.setMode(SqlItem.DELETED);
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newTable.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Deletion);
	}

	public static void attributeTypeChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterAttr();
		updateAttribute(newAttr, "TypeChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(),
				ChangeType.AttrTypeChange);
	}

	public static void attributeKeyChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterKey();
		updateAttribute(newAttr, "KeyChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(),
				ChangeType.KeyChange);
	}

	public static void deleteTable(Table t) {
		deleteItem(t);
		results.myMetrics.deleteTable();
		markAll(t, SqlItem.DELETED); // mark attributes deleted
	}

	public static void insertTable(Table t) {
		insertItemInList(t);
		results.myMetrics.insetTable();
		markAll(t, SqlItem.INSERTED); // mark attributes inserted
	}

	public static void alterTable(Table t) {
		results.myMetrics.alterTable();
	}

	public static void match(SqlItem oldI, SqlItem newI) {
		oldI.setMode(SqlItem.MACHED);
		newI.setMode(SqlItem.MACHED);
	}

	public static void markAll(Table t, int mode) {
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

	public static void insertItemInList(SqlItem item) {
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

	public static void deleteItem(SqlItem item) {
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

	public static void updateAttribute(Attribute item, String type) {
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

	public static void setOriginalSizes(int[] sizeA, int[] sizeB) {
		results.myMetrics.setOrigTables(sizeA[0]);
		results.myMetrics.setOrigAttrs(sizeA[1]);
		results.myMetrics.setNewTables(sizeB[0]);
		results.myMetrics.setNewAttrs(sizeB[1]);
	}

	// Personal Project Below -0---o-0-0---
//	public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
//		int len0 = lhs.length() + 1;
//		int len1 = rhs.length() + 1;
//
//		// the array of distances
//		int[] cost = new int[len0];
//		int[] newcost = new int[len0];
//
//		// initial cost of skipping prefix in String s0
//		for (int i = 0; i < len0; i++)
//			cost[i] = i;
//
//		// dynamically computing the array of distances
//
//		// transformation cost for each letter in s1
//		for (int j = 1; j < len1; j++) {
//			// initial cost of skipping prefix in String s1
//			newcost[0] = j;
//
//			// transformation cost for each letter in s0
//			for (int i = 1; i < len0; i++) {
//				// matching current letters in both strings
//				int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;
//
//				// computing cost for each transformation
//				int cost_replace = cost[i - 1] + match;
//				int cost_insert = cost[i] + 1;
//				int cost_delete = newcost[i - 1] + 1;
//
//				// keep minimum cost
//				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
//			}
//
//			// swap cost/newcost arrays
//			int[] swap = cost;
//			cost = newcost;
//			newcost = swap;
//		}
//
//		// the distance is the cost for transforming all letters in both strings
//		return cost[len0 - 1];
//	}
//
//	public static void getLevenshteinDistance(String nameA, String nameB) {
//
//		int length, lengthA, lengthB;
//		int distance = 0;
//		double threshold = 0.20;
//
//		lengthA = nameA.length();
//		lengthB = nameB.length();
//
//		for (int i = 0; i < lengthA; i++) {
//			for (int j = 0; j < lengthB; j++) {
//				if (nameA.charAt(i) != nameB.charAt(i)) {
//					distance += 1;
//				}
//			}
//
//		}
		// if (lengthA > lengthB){
		// length = lengthB;
		// distance = lengthA - lengthB;
		// }
		// else {
		// length = lengthA;
		// distance = lengthB - lengthA;
		// }
		//// System.out.println(distance);
		////
		//// System.out.println(nameB);
		// while( i < length){
		// if (nameA.charAt(i) != nameB.charAt(i)){
		// distance +=1;
		// }
		// i=i+1;
		// }
		// //&& distance <=0.5*length
		// if( distance > 1){
		// System.out.println("---------------------");
		//
		// System.out.println("distance : "+distance+"\n");
		// System.out.println("threshold*length : "+0.5*length+"\n");
		// System.out.println("nameA : "+nameA+"\n");
		// System.out.println("nameB : "+nameB+"\n");
		//
		// }

		// return distance;

//	}

}
