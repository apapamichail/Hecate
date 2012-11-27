/**
 * 
 */
package gr.uoi.cs.dmod.hecate.transitions;

import gr.uoi.cs.dmod.hecate.sql.Attribute;
import gr.uoi.cs.dmod.hecate.sql.Table;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author iskoulis
 *
 */
@XmlRootElement
public class Insersion implements Transition {

	@XmlElement(name="table")
	Table affectedTable;
	@XmlElement(name="attribute")
	Collection<Attribute> insertedAtributes;
	@XmlAttribute(name="type")
	String type;
	
	public Insersion() {
		affectedTable = null;
		type = null;
		insertedAtributes = new ArrayList<Attribute>();
	}
	
	public Table getAffTable(){
		return affectedTable;
	}
	
	public int getNumOfAffAttributes() {
		return insertedAtributes.size();
	}
	
	public Collection<Attribute> getAffAttributes() {
		return insertedAtributes;
	}
	
	public void insertAttribute(Attribute newAttribute) throws Exception {
		if (type == null) {
			type = "UpdateTable";
		}
		if (affectedTable == null) {
			this.affectedTable = newAttribute.getTable();
		} else if (affectedTable != newAttribute.getTable()){
			throw new Exception("ta ekanes salata!");
		}
		this.insertedAtributes.add(newAttribute);
	}
	
	public void insertTable(Table newTable) {
		this.type = "NewTable";
		this.affectedTable = newTable;
		this.insertedAtributes = newTable.getAttrs().values();
	}
}
