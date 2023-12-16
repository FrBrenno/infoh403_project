import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class ASTGenerator {
    private ParseTree parseTree;

    public ASTGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    public AST generateAST() {
        return generateAST(parseTree);
    }

    private AST generateAST(ParseTree parseTree) {
        AST ast = new AST(parseTree.getLabel());
        for (ParseTree child : parseTree.getChildren()) {
            Symbol currentSymbol = child.getLabel();
            if (currentSymbol.isTerminal()) {
                ast.addChild(new AST(child.getLabel()));
            }
            else {
                // Variables
                if (child.getLabel().getType() == LexicalUnit.CODE)
                {
                    List<ParseTree> grandChildren = generateAST(child).getChildren();
                    System.out.println(grandChildren.size());
                    for (ParseTree codeChild : grandChildren)
                    {
                        ast.addChild(codeChild);
                    }
                }
                else {
                    ast.addChild(generateAST(child));
                }
            }
        }
        return ast;
    }
}
