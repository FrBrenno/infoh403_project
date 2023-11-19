import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private FileReader inputFile;
    private LexicalAnalyzer lexer;
    private Symbol currentToken;
    private Symbol lookAhead;
    private ArrayList<Integer> usedRules;
    

    public Parser(String filename) throws IOException {
        try {
            inputFile = new FileReader(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lexer = new LexicalAnalyzer(inputFile);
        lookAhead = lexer.yylex();
        nextToken();
        usedRules = new ArrayList<Integer>();
    }

    public Symbol getCurrentToken() {
        return currentToken;
    }

    public void nextToken() {
        currentToken = lookAhead;
        try {
            lookAhead = lexer.yylex();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printToken() {
        /*Pas super utile, juste pour que mon monkey brain capte ce qu'il se passe hahaha */
        /*AHHAHAHAHAH tkt, ça m'est aussi utile pour comprendre*/
        System.out.println("lookahead ---> "+currentToken.toString());
    }

    /* 
    [1] <Program> -> <Code>
    First de <Program> : begin 
    */
    public ParseTree program() {
        printToken();
        Symbol progSymbol = new Symbol(LexicalUnit.PROGRAM);
        ParseTree root = new ParseTree(progSymbol);
        switch (currentToken.getType()) {
            case BEG:
                usedRules.add(1);

                ParseTree begintree = new ParseTree(currentToken);                  root.addChild(begintree);
// ___________________________________________________________________________________________________________________________________
                ParseTree codeTree = new ParseTree(new Symbol(LexicalUnit.CODE));   root.addChild(code(codeTree));  //les returns de chaque trucs doivent rendre leur arbres (plutot que true false)
// ___________________________________________________________________________________________________________________________________
                ParseTree endtree = new ParseTree(new Symbol(LexicalUnit.END));     root.addChild(endtree);
                System.out.println(root.toForestPicture());
                return root;
        
            default:
                syntaxError(currentToken);
                return null;
        }
    }

    /* 
     * [2] <Code> -> <InstList> 
     * [3] <Code> -> ε
     * First de <Code> : begin, read, print, while, if, [VarName], ε
     */
    private ParseTree code(ParseTree ParentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME || currentToken.getType() == LexicalUnit.BEG
                || currentToken.getType() == LexicalUnit.IF || currentToken.getType() == LexicalUnit.WHILE
                || currentToken.getType() == LexicalUnit.PRINT || currentToken.getType() == LexicalUnit.READ) 
                /* la condition parait super moche mais c'est parce que tout ces tokens 
                utilisent la règle 2, avec le switch case c'était beaucoup long et
                il y avait beaucoup de répétitions
                */
                { 
            usedRules.add(2);
            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            ParentTree.addChild(instList(instListTree));

            return ParentTree;

        } else if (currentToken.getType() == LexicalUnit.END) {
            usedRules.add(3);
            // ICI il faut rien mettre car on a déjà consommé le END dans le switch de program (pour les epsilon juste mettre la regle quoi)
            return null;
        } else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
     * [4] <InstList> -> <Inst> <InstTail>
     * First de <InstList> : begin, read, print, while, if, [VarName]
     */
    private ParseTree instList(ParseTree ParentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME || currentToken.getType() == LexicalUnit.BEG
                || currentToken.getType() == LexicalUnit.IF || currentToken.getType() == LexicalUnit.WHILE
                || currentToken.getType() == LexicalUnit.PRINT || currentToken.getType() == LexicalUnit.READ) {
            usedRules.add(4);
            ParseTree instTree = new ParseTree(new Symbol(LexicalUnit.INST));
            ParentTree.addChild(inst(instTree));
            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            ParentTree.addChild(instTail(instListTree));
            return ParentTree;
        } 
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
     */
    private ParseTree instTail(ParseTree ParentTree) {
        return null;
    }
    
    /* 
    [7] <Instruction>   →	<Assign>
	[8]                 →	<If>
	[9]                 →	while<Cond>do<Instruction>
	[10]                →	print([varName])
	[11]                →	read([varName])
	[12]                →	begin<InstList>end
     
    * First de inst: begin, read, print, while, if, [VarName]
     */
    private ParseTree inst(ParseTree Parenttree) {
        nextToken();
        printToken();
        if(currentToken.getType() == LexicalUnit.BEG ){
            usedRules.add(12);
            ParseTree begintree = new ParseTree(currentToken);                  
            Parenttree.addChild(begintree);
            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            Parenttree.addChild(instList(instListTree));
            ParseTree endTree = new ParseTree(new Symbol(LexicalUnit.END));
            Parenttree.addChild(endTree);
            return Parenttree;
        }
        if (currentToken.getType() == LexicalUnit.VARNAME) {
            usedRules.add(7);
            ParseTree assignTree = new ParseTree(new Symbol(LexicalUnit.ASSIGN));
            Parenttree.addChild(assign(assignTree));
            return Parenttree;
        }
        if (currentToken.getType() == LexicalUnit.READ) { // c'est un bordel, mais laisse le moi quand je reviens du sport
            usedRules.add(11);
            ParseTree readTree = new ParseTree(new Symbol(LexicalUnit.READ));
            nextToken();
            printToken();
            if (currentToken.getType() == LexicalUnit.LPAREN) {
                ParseTree lparenTree = new ParseTree(currentToken);
                Parenttree.addChild(lparenTree);
                nextToken();
                printToken();
                if (currentToken.getType() == LexicalUnit.VARNAME) {
                    ParseTree varNameTree = new ParseTree(currentToken);
                    Parenttree.addChild(varNameTree);
                    nextToken();
                    printToken();
                    if (currentToken.getType() == LexicalUnit.RPAREN) {
                        ParseTree rparenTree = new ParseTree(currentToken);
                        Parenttree.addChild(rparenTree);
                        
                        return Parenttree;
                    }
                    else {
                        syntaxError(currentToken);
                        return null;
                    }
                }
                else {
                    syntaxError(currentToken);
                    return null;
                }
            }
            else {
                syntaxError(currentToken);
                return null;
            }
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }
    
    private ParseTree read(ParseTree readTree) {
        return null;
    }

    /* 
     * [13]	<Assign>	→	[VarName] :=<ExprArith>
     * 
     * First de assign: [VarName]
     */
    private ParseTree assign(ParseTree ParentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME) {
            usedRules.add(13);
            ParseTree varNameTree = new ParseTree(currentToken);
            ParentTree.addChild(varNameTree);
            ParseTree equalTree = new ParseTree(new Symbol(LexicalUnit.EQUAL));
            ParentTree.addChild(equalTree);
            ParseTree exprTree = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
            ParentTree.addChild(expr(exprTree));
            return ParentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    private ParseTree expr(ParseTree exprTree) {
        return null;
    }

    private void syntaxError(Symbol token) {
        System.out.println("Oh no ! Syntax error on line " + token.getLine() + " column " + token.getColumn() + " : " + token.getValue());
        //System.out.println("Expected token : " + token.getType());
        System.exit(1);
    }
}