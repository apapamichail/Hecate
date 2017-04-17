package gr.uoi.cs.daintiness.hecate.diff;

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

public class SchemataDifferencesTools {
	
	protected static Insersion in;
	protected static Deletion out;
	protected static Update up;
	protected static Iterator<String> oldAttributeKeys ;
	protected static Iterator<Attribute> oldAttributeValues ;
	protected static Iterator<String> newAttributeKeys;
	protected static Iterator<Attribute> newAttributeValues;
	protected static Iterator<String> oldTableKeys;
	protected static Iterator<Table> oldTableValues;
	protected static Iterator<String> newTableKeys;
	protected static Iterator<Table> newTableValues;
	protected static DifferencesResult results;

	protected static void setUp(Schema schemaA,Schema schemaB){
		
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
		
	}
	protected static void checkRemainingTableKeysforNew() {
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
	protected static void checkRemainingTableKeysforOld() {
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
	protected static void insertAttributesNotInOld(Table oldTable) {
		String newAttrKey;
		newAttrKey = (String) newAttributeKeys.next();
		Attribute newAttr = newAttributeValues.next();
		attributeInsert(oldTable, newAttr);
	}
	
	/**
	 * @param newTable
	 */
	protected static void deleteAttributesNotInNew(Table newTable) {
		String oldAttrKey;
		oldAttrKey = (String) oldAttributeKeys.next();
		Attribute oldAttr = oldAttributeValues.next();
		attributeDelete(oldAttr, newTable);
	}
 
	/**
	 * @param oldTable
	 * @param newTable
	 */
	protected static void initializeAttributesValues(Table oldTable, Table newTable) {
		oldAttributeValues = oldTable.getAttrs().values().iterator() ;
		newAttributeValues = newTable.getAttrs().values().iterator() ;
	}

	/**
	 * @param oldTable
	 * @param newTable
	 */
	protected static void initializeAttributesKeys(Table oldTable, Table newTable) {
		oldAttributeKeys = oldTable.getAttrs().keySet().iterator();
		newAttributeKeys = newTable.getAttrs().keySet().iterator();
	}

	

	protected static void attributeInsert(Table oldTable, Attribute newAttr) {
		results.myMetrics.insertAttr();
		insertItemInList(newAttr);
		newAttr.setMode(SqlItem.INSERTED);
		oldTable.setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(oldTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Insertion);
	}

	protected static void attributeDelete(Attribute oldAttr, Table newTable) {
		results.myMetrics.deleteAttr();
		deleteItem(oldAttr);
		oldAttr.setMode(SqlItem.DELETED);
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newTable.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Deletion);
	}

	protected static void attributeTypeChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterAttr();
		updateAttribute(newAttr, "TypeChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(), ChangeType.AttrTypeChange);
	}

	protected static void attributeKeyChange(Attribute oldAttr, Attribute newAttr) {
		results.myMetrics.alterKey();
		updateAttribute(newAttr, "KeyChange");
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		oldAttr.setMode(SqlItem.UPDATED);
		newAttr.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newAttr.getTable().getName(), results.myMetrics.getNumRevisions(), ChangeType.KeyChange);
	}
	
	protected static void deleteTable(Table t) {
		deleteItem(t);
		results.myMetrics.deleteTable();
		markAll(t, SqlItem.DELETED);     // mark attributes deleted
	}
	
	protected static void insertTable(Table t) {
		insertItemInList(t);
		results.myMetrics.insetTable();
		markAll(t, SqlItem.INSERTED);     // mark attributes inserted
	}
	
	protected static void alterTable(Table t) {
		results.myMetrics.alterTable();
	}

	protected static void match(SqlItem oldI, SqlItem newI) {
		oldI.setMode(SqlItem.MACHED);
		newI.setMode(SqlItem.MACHED);
	}

	protected static void markAll(Table t, int mode) {
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
	
	protected static void insertItemInList(SqlItem item) {
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
	
	protected static void deleteItem(SqlItem item) {
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
	
	protected static void updateAttribute(Attribute item, String type) {
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
	
	protected static void setOriginalSizes(int[] sizeA, int[] sizeB) {
		results.myMetrics.setOrigTables(sizeA[0]); results.myMetrics.setOrigAttrs(sizeA[1]);
		results.myMetrics.setNewTables(sizeB[0]); results.myMetrics.setNewAttrs(sizeB[1]);
	}
}
