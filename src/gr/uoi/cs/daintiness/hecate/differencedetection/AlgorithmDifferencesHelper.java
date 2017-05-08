package gr.uoi.cs.daintiness.hecate.differencedetection;

import java.util.Iterator;

import gr.uoi.cs.daintiness.hecate.metrics.tables.ChangeType;
import gr.uoi.cs.daintiness.hecate.sql.Attribute;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.SqlItem;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Deletion;
import gr.uoi.cs.daintiness.hecate.transitions.Insersion;
import gr.uoi.cs.daintiness.hecate.transitions.Update;

public class AlgorithmDifferencesHelper {

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
//	
	public  void checkRemainingTableKeys(Iterator<String> tableKeys, Iterator<Table>  tableValues, String action) {
		String tableKey;
		while (tableKeys.hasNext()) {
			tableKey = (String) tableKeys.next();
			Table table = (Table) tableValues.next();
			if(action.equals(insertAction)){
				insertTable(table);
			}
			else if(action.equals(deleteAction)){
				deleteTable(table);
			}
		}
	}
	
	
	
	public  void updateAttributesInList(Table table,Iterator<String> attributeKeys, Iterator<Attribute> attributeValues, String action) {
		String attrKey;
		attrKey = (String) attributeKeys.next();
		Attribute attr = attributeValues.next();
		if (action.equals("insert"))
			attributeInsert(table, attr);
		else if (action.equals("delete"))
			attributeDelete(table, attr);
	}

	public  void initializeAttributesValues(Table oldTable, Table newTable) {
		oldAttributeValues = oldTable.getAttrs().values().iterator();
		newAttributeValues = newTable.getAttrs().values().iterator();
	}

	public  void initializeAttributesKeys(Table oldTable, Table newTable) {
		oldAttributeKeys = oldTable.getAttrs().keySet().iterator();
		newAttributeKeys = newTable.getAttrs().keySet().iterator();
	}


	public  void attributeInsert(Table oldTable, Attribute newAttr) {
		results.myMetrics.insertAttr();
		insertItemInList(newAttr);
		newAttr.setMode(SqlItem.INSERTED);
		oldTable.setMode(SqlItem.UPDATED);
		newAttr.getTable().setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(oldTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Insertion);
	}


	public  void attributeDelete( Table newTable, Attribute oldAttr) {
		results.myMetrics.deleteAttr();
		deleteItem(oldAttr);
		oldAttr.setMode(SqlItem.DELETED);
		oldAttr.getTable().setMode(SqlItem.UPDATED);
		newTable.setMode(SqlItem.UPDATED);
		results.tablesInfo.addChange(newTable.getName(), results.myMetrics.getNumRevisions(), ChangeType.Deletion);
	}

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

	public  void deleteTable(Table t) {
		deleteItem(t);
		results.myMetrics.deleteTable();
		markAll(t, SqlItem.DELETED); // mark attributes deleted
	}


	public  void insertTable(Table t) {
		insertItemInList(t);
		results.myMetrics.insetTable();
		markAll(t, SqlItem.INSERTED); // mark attributes inserted
	}

	
	public  void alterTable(Table t) {
		results.myMetrics.alterTable();
	}


	public  void match(SqlItem oldI, SqlItem newI) {
		oldI.setMode(SqlItem.MACHED);
		newI.setMode(SqlItem.MACHED);
	}


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


	public  void setOriginalSizes(int[] sizeA, int[] sizeB) {
		results.myMetrics.setOrigTables(sizeA[0]);
		results.myMetrics.setOrigAttrs(sizeA[1]);
		results.myMetrics.setNewTables(sizeB[0]);
		results.myMetrics.setNewAttrs(sizeB[1]);
	}

}
