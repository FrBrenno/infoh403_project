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

    /**
     * Generates an AST from a parse tree.
     * @param parseTree the parse tree
     * @return the AST
     */
     public AST generateAST(ParseTree parseTree) {
        if (parseTree == null) {
            return null;
        }
        else if (parseTree.getChildren().isEmpty()) {
            return new AST(parseTree.getLabel());
        }
        else {
            List<ParseTree> children = new ArrayList<ParseTree>();
            for (ParseTree child : parseTree.getChildren()) {
                children.add(generateAST(child));
            }
            return new AST(parseTree.getLabel(), children);
        }
     }
}
