import java.util.ArrayList;
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
                if (ignoreVariable(child.getLabel().getType()))
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

    /**
	 * This function is used to ignore
	 * the following variables that we don't want
	 * to add to the AST
	 */
	private boolean ignoreVariable(LexicalUnit type){
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
		//variables.add(LexicalUnit.PROD);
		//variables.add(LexicalUnit.ATOM);
		// variables.add(LexicalUnit.CONDATOM);
		// variables.add(LexicalUnit.AND);
		
		if (variables.contains(type)) {
			return true;
		}
		else {
			return false;
		}	
	}
}
