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
            if (child.getChildren().isEmpty()) { //ici on sait que c'est une feuille
                switch(child.getLabel().getType()){ //tout les switchs des terminaux qu'on veut ignorer ou des variables qui vont vers epsilon
                    case LBRACK:
                        break;
                    case RBRACK:
                        break;
                    case LPAREN:
                        break;
                    case RPAREN:
                        break;
                    case PRODPRIME:
                        break;
                    case EXPRARITPRIME:
                        break;
                    case ANDPRIME:
                        break;
                    case CONDPRIME:
                        break;
                    case INSTTAIL:  
                        break;
                    case ELSETAIL:
                        break;
                    case READ:
                        break;
                    case DOTS:
                        break;
                    case PRINT:
                        break;

                    default :
                        ast.addChild(new AST(child.getLabel()));
                }
            }
            else {
                // Variables
                if (child.getLabel().isVariable())
                {
                    List<ParseTree> grandChildren = generateAST(child).getChildren();
                    for (ParseTree elem : grandChildren)
                    {
                        ast.addChild(elem);
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