package gr.uoi.cs.daintiness.hecate.parser;

import gr.uoi.cs.daintiness.hecate.sql.Attribute;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * This class uses the parser of the generated ANTLR grammar to parse
 * the file given to the constructor.
 * @author giskou
 */
public class HecateParser implements ParserInterface {
	static Schema schema;

	static class UnMach {
		Table orT;
		DDLParser.ForeignContext ctx;
		
		public UnMach(Table orT, DDLParser.ForeignContext ctx) {
			this.orT = orT;
			this.ctx = ctx;
		}
	}

	/**
	 * 
	 * @param filePath The path of the file to be parsed.
	 * @throws IOException
	 * @throws RecognitionException
	 */
	public static Schema parse(String filePath) {
		CharStream      charStream = null;
		
		try {
			charStream = new ANTLRFileStream(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		DDLLexer        lexer = new DDLLexer(charStream) ;
		TokenStream     tokenStream = new CommonTokenStream(lexer);
		DDLParser       parser = new DDLParser(tokenStream) ;
		ParseTree       root = parser.start();
		SchemaLoader    loader = new SchemaLoader();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(loader, root);
		File file = new File(filePath);
		schema.setTitle(file.getName());
		return schema;
	}
	public static void parseSchema(String filePath) {
		CharStream      charStream = null;
		
		try {
			charStream = new ANTLRFileStream(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		DDLLexer        lexer = new DDLLexer(charStream) ;
		TokenStream     tokenStream = new CommonTokenStream(lexer);
		DDLParser       parser = new DDLParser(tokenStream) ;
		ParseTree       root = parser.start();
		SchemaLoader    loader = new SchemaLoader();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(loader, root);
		File file = new File(filePath);
		schema.setTitle(file.getName());
		
	}
	private static class SchemaLoader extends DDLBaseListener {

		private Table t;
		private Attribute a;
		boolean foundTableConst = false;
		boolean foundAlterConst = false;
		boolean foundLineConst = false;
		String alteringTable = null;
		List<UnMach> unMached = new ArrayList<HecateParser.UnMach>();

		public void enterStart (DDLParser.StartContext ctx) {
//			System.out.println("Starting");
			schema = new Schema();
		}
		public void exitStart (DDLParser.StartContext ctx) {
			processUnmached();
//			System.out.println("\n\n\n" + schema.print());
		}

		public void enterTable (DDLParser.TableContext ctx) {
			String tableName = removeQuotes(ctx.table_name().getText());
			t = new Table(tableName);
		}
		public void exitTable (DDLParser.TableContext ctx) {
			schema.addTable(t);
		}

		public void enterColumn (DDLParser.ColumnContext ctx) {
			String colName = removeQuotes(ctx.col_name().getText());
			String colType = ctx.data_type().getText();
			colType = colType.toUpperCase();
			a = new Attribute(colName, colType);
		}

		public void exitColumn (DDLParser.ColumnContext ctx) {
			t.addAttribute(a);
		}

		public void enterLine_constraint(DDLParser.Line_constraintContext ctx) {
			foundLineConst = true;
		}
		public void exitLine_constraint(DDLParser.Line_constraintContext ctx) {
			foundLineConst = false;
		}

		public void enterAlter_statement(DDLParser.Alter_statementContext ctx) {
			alteringTable = ctx.table_name().getText();
		}
		public void exitAlter_statement(DDLParser.Alter_statementContext ctx) {
			alteringTable = null;
		}

		public void enterTable_constraint(DDLParser.Table_constraintContext ctx) {
			foundTableConst = true;
		}
		public void exitTable_constraint(DDLParser.Table_constraintContext ctx) {
			foundTableConst = false;
		}

		public void enterAlter_constraint(DDLParser.Alter_constraintContext ctx) {
			foundAlterConst = true;
		}
		public void exitAlter_constraint(DDLParser.Alter_constraintContext ctx) {
			foundAlterConst = true;
		}

		public void enterPrimary (DDLParser.PrimaryContext ctx) {
			if (foundTableConst) {
				String todo = ctx.parNameList().getText();
				todo = todo.substring(1, todo.length()-1);
				String[] names = todo.split(",");
				for (String schema : names) {
					if (t.getAttrs().get(schema) != null){
						t.addAttrToPrimeKey(t.getAttrs().get(schema));
					}
				}
			} else if (foundAlterConst) {
			} else if (foundLineConst){
				t.addAttrToPrimeKey(a);
			} else {}
		}

		public void enterForeign (DDLParser.ForeignContext ctx) {
			Table orTable = null, reTable = null;
			Attribute[] or, re;
			String reTableName = ctx.reference_definition().table_name().getText();
			if (foundTableConst) {
				orTable = t;
				if (reTableName.compareTo(orTable.getName()) == 0) {
					reTable = t;
				} else {
					reTable = schema.getTables().get(reTableName);
					if (reTable == null) {
						unMached.add(new UnMach(orTable, ctx));
						return;
					}
				}
			} else if (foundAlterConst) {
//				System.out.println(alteringTable + " " + ctx.getText());
				orTable = schema.getTables().get(alteringTable);
				reTable = schema.getTables().get(reTableName);
			} else {
				// this is not supposed to happen
			}
			or = getNames(ctx.parNameList().getText(), orTable);
			re = getNames(ctx.reference_definition().parNameList().getText(), reTable);
			for (int i = 0; i < or.length; i++) {
				if (or[i] == null || re[i] == null) {
					// sql typo???
					continue;
				}
//				System.out.println(orTable + "." + or[i] + "->" + reTable + "." + re[i] + "\n");
				orTable.getfKey().addReference(or[i], re[i]);
			}
		}

		public void enterReference (DDLParser.ReferenceContext ctx) {
			Table orTable = t;
			Table reTable = schema.getTables().get(ctx.reference_definition().table_name().getText());
			Attribute or = a;
			Attribute[] re = getNames(ctx.reference_definition().parNameList().getText(), reTable);
			orTable.getfKey().addReference(or, re[0]);
		}

		private void processUnmached() {
			for (UnMach item : unMached) {
				DDLParser.ForeignContext ctx = item.ctx;
				Table orTable = item.orT;
				String reTableName = ctx.reference_definition().table_name().getText();
				Table reTable = schema.getTables().get(reTableName);
				if (reTable == null) {
					// still not found ... ignore
					continue;
				}
				Attribute[] or = getNames(ctx.parNameList().getText(), orTable);
				Attribute[] re = getNames(ctx.reference_definition().parNameList().getText(), reTable);
				for (int i = 0; i < or.length; i++) {
					if (or[i] == null || re[i] == null) {
						// sql typo???
						continue;
					}
//					System.out.println(orTable + "." + or[i] + "->" + reTable + "." + re[i] + "\n");
					orTable.getfKey().addReference(or[i], re[i]);
				}
			}
		}

		private Attribute[] getNames(String schema, Table table) {
			schema = schema.substring(1, schema.length()-1);
			String[] names = schema.split(",");
			Attribute[] res = new Attribute[names.length];
			for (int i = 0; i < names.length; i++) {
				res[i] = table.getAttrs().get(names[i]);
			}
			return res;
		}

		private boolean hasQuotes(String schema) {
			switch (schema.charAt(0)) {
				case '\'':
				case '\"':
				case '`':
					return true;
				default:
					return false;
			}
		}

		private String removeQuotes(String schema) {
			if (hasQuotes(schema)) {
				String res = null;
				res = schema.substring(1, schema.length()-1);
				return res;
			}
			return schema;
		}
	}
}
