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
                if (!ignoreLeaf(child.getLabel().getType())) { // it is not ignored, add it to the AST
                    ast.addChild(new ParseTree(child.getLabel()));
                }
            }
            else {// Variables
                if (ignoreVariable(child.getLabel().getType())) // it ignored, get the children and check
                {
                    List<ParseTree> grandChildren = generateAST(child).getChildren();
                    for (ParseTree elem : grandChildren)
                    {
                        ast.addChild(elem);
                    }
                }
                else { // it is not ignored, add it to the AST
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

        return variables.contains(type);
	}

    /**
	 * This function is used to ignore
	 * the following leaf node that we don't want
	 * to add to the AST
	 */
	private boolean ignoreLeaf(LexicalUnit type){
		List<LexicalUnit> leafs = new ArrayList<LexicalUnit>();
        // Leaf Terminals
        leafs.add(LexicalUnit.LBRACK);
        leafs.add(LexicalUnit.RBRACK);
        leafs.add(LexicalUnit.LPAREN);
        leafs.add(LexicalUnit.RPAREN);
        leafs.add(LexicalUnit.READ);
        leafs.add(LexicalUnit.DOTS);
        leafs.add(LexicalUnit.PRINT);
        leafs.add(LexicalUnit.BEG);
        leafs.add(LexicalUnit.END);
        leafs.add(LexicalUnit.THEN);
        leafs.add(LexicalUnit.ELSE);
        leafs.add(LexicalUnit.IF);
        leafs.add(LexicalUnit.ASSIGN);
        leafs.add(LexicalUnit.WHILE);
        leafs.add(LexicalUnit.DO);
        // Leafs variables
        leafs.add(LexicalUnit.PRODPRIME);
        leafs.add(LexicalUnit.EXPRARITPRIME);
        leafs.add(LexicalUnit.ANDPRIME);
        leafs.add(LexicalUnit.CONDPRIME);
        leafs.add(LexicalUnit.INSTTAIL); 
        leafs.add(LexicalUnit.ELSETAIL);
        
        return leafs.contains(type);
	}
}
