import java.util.ArrayList;
import java.util.List;

public class AST extends ParseTree {

    public AST(Symbol label) {
        super(label);
    }
    
    public AST(Symbol label, List<ParseTree> children) {
        // An instance of AST is also an instance of ParseTree (due to inheritance).
        // You can use an object of type AST wherever an object of type ParseTree is expected.
        // Whenever calling this constructor, you can pass a list of ASTs as the children.
        super(label, children);
    }

}
