import java.util.ArrayList;
import java.util.List;

public class ASTGenerator {
    public AST generateAST(ParseTree parseTree) {
        return new AST(parseTree.getLabel(), parseTree.getChildren());
    }
}
