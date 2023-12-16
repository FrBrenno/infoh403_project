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

	public boolean isTerminal(){
		List<LexicalUnit> terminals = new ArrayList<LexicalUnit>();
		terminals.add(LexicalUnit.VARNAME);
		terminals.add(LexicalUnit.ASSIGN);
		terminals.add(LexicalUnit.NUMBER);
		terminals.add(LexicalUnit.BEG);
		terminals.add(LexicalUnit.END);
		terminals.add(LexicalUnit.DOTS);
		terminals.add(LexicalUnit.LPAREN); // OUI NON ?
		terminals.add(LexicalUnit.RPAREN); // OUI NON ?
		terminals.add(LexicalUnit.MINUS);
		terminals.add(LexicalUnit.PLUS);
		terminals.add(LexicalUnit.TIMES);
		terminals.add(LexicalUnit.DIVIDE);
		terminals.add(LexicalUnit.IF);
		terminals.add(LexicalUnit.THEN);
		terminals.add(LexicalUnit.ELSE);
		terminals.add(LexicalUnit.AND);
		terminals.add(LexicalUnit.OR);
		terminals.add(LexicalUnit.LBRACK);
		terminals.add(LexicalUnit.RBRACK);
		terminals.add(LexicalUnit.EQUAL);
		terminals.add(LexicalUnit.SMALLER);
		terminals.add(LexicalUnit.WHILE);
		terminals.add(LexicalUnit.DO);
		terminals.add(LexicalUnit.PRINT);
		terminals.add(LexicalUnit.READ);
		terminals.add(LexicalUnit.COND);
		
		if (terminals.contains(this.type)) {
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
