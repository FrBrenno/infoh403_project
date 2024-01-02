import java.util.ArrayList;
import java.util.List;

public class Symbol{
	public static final int UNDEFINED_POSITION = -1;
	public static final Object NO_VALUE = null;
	
	private final LexicalUnit type;
	private final Object value;
	private final int line,column;

	public Symbol(LexicalUnit unit,int line,int column,Object value){
		this.type	= unit;
		this.line	= line+1;
		this.column	= column;
		this.value	= value;
	}
	
	public Symbol(LexicalUnit unit,int line,int column){
		this(unit,line,column,NO_VALUE);
	}
	
	public Symbol(LexicalUnit unit,int line){
		this(unit,line,UNDEFINED_POSITION,NO_VALUE);
	}

	public Symbol(LexicalUnit unit){
		this(unit,UNDEFINED_POSITION,UNDEFINED_POSITION,NO_VALUE);
	}

	public Symbol(LexicalUnit unit,Object value){
		this(unit,UNDEFINED_POSITION,UNDEFINED_POSITION,value);
	}

	/**
	 * This function is used to ignore
	 * the following variables that we don't want
	 * to add to the AST
	 */
	public boolean ignoreVariable(){
		List<LexicalUnit> variables = new ArrayList<LexicalUnit>();
		variables.add(LexicalUnit.CODE);
		variables.add(LexicalUnit.EXPRARITPRIME);
		variables.add(LexicalUnit.PRODPRIME);
		variables.add(LexicalUnit.CONDPRIME);
		variables.add(LexicalUnit.ANDPRIME);
		variables.add(LexicalUnit.COMP);
		variables.add(LexicalUnit.EOS);
		variables.add(LexicalUnit.INSTTAIL);
		variables.add(LexicalUnit.INST); 
		
		if (variables.contains(this.type)) {
			return true;
		}
		else {
			return false;
		}	
	}
	
	
	public LexicalUnit getType(){
		return this.type;
	}
	
	public Object getValue(){
		return this.value;
	}
	
	public int getLine(){
		return this.line;
	}
	
	public int getColumn(){
		return this.column;
	}
	
	@Override
	public int hashCode(){
		final String value	= this.value != null? this.value.toString() : "null";
		final String type		= this.type  != null? this.type.toString()  : "null";
		return new String(value+"_"+type).hashCode();
	}
	
	@Override
	public String toString(){
		final String value	= this.value != null? this.value.toString() : "null";
		final String type		= this.type  != null? this.type.toString()  : "null";
		return "token: "+value+"\tlexical unit: "+type;
	}

	public String toTreeString(){
		final String value	= this.value != null? this.value.toString() : "null";
		final String type		= this.type  != null? this.type.toString()  : "null";
		if(value == "null"){
			return type;
		}
		return type+" value: "+value;
		}
}
