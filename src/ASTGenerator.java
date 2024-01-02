import java.util.List;

public class ASTGenerator {
    private ParseTree parseTree;

    public ASTGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    public ParseTree generateAST() {
        return generateAST(parseTree);
    }

    /**
     *  Generates the AST from the parse tree
     * @param parseTree derivation tree from parser
     * @return AST
     */
    private ParseTree generateAST(ParseTree parseTree) {
        ParseTree ast = new ParseTree(parseTree.getLabel());
        for (ParseTree child : parseTree.getChildren()) {
            if (child.getChildren().isEmpty()) {     //so it's a leaf
                switch(child.getLabel().getType()){  //the switch is for terminals that can be ignored or variables that goes to epsilon
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
                    case BEG:
                        break;
                    case END:
                        break;
                    case THEN:
                        break;
                    case ELSE:
                        break;
                    case IF:
                        break;
                    case ASSIGN:
                        break;
                    case WHILE:
                        break;
                    case DO:
                        break;
                    
                    default :
                        ast.addChild(new ParseTree(child.getLabel()));
                }
            }
            else {
                // Variables
                if (child.getLabel().ignoreVariable())
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
