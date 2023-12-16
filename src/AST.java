import java.util.List;

public class AST extends ParseTree {

    public AST(Symbol lbl) {
        super(lbl);
        //TODO Auto-generated constructor stub
    }
    
    public AST(Symbol lbl, List<AST> chdn) {
        super(lbl, (List<ParseTree>) chdn);
        
    }
}
