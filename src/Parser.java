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
        if (currentToken.getType() == LexicalUnit.BEG)
        {
            usedRules.add(1);
            ParseTree beginTree = new ParseTree(currentToken);
            root.addChild(beginTree);

            ParseTree codeTree = new ParseTree(new Symbol(LexicalUnit.CODE));
            root.addChild(
                    code(codeTree)
            );

            ParseTree endTree = new ParseTree(new Symbol(LexicalUnit.END));
            root.addChild(endTree);

            System.out.println(root.toForestPicture());
            return root;
        }
        syntaxError(currentToken);
        return null;
    }

    /*
     * [2] <Code> -> <InstList>
     * [3] <Code> -> ε
     * First de <Code> : [VarName], begin, if, while, print, read, ε
     */
    private ParseTree code(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.BEG ||
            currentToken.getType() == LexicalUnit.IF ||
            currentToken.getType() == LexicalUnit.WHILE ||
            currentToken.getType() == LexicalUnit.PRINT ||
            currentToken.getType() == LexicalUnit.READ
        ){
            usedRules.add(2);
            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.END) {
            usedRules.add(3);
            return null;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
     * [4] <InstList> -> <Inst> <InstTail>
     * First de <InstList> : [VarName], begin, if, while, print, read
     */
    private ParseTree instList(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.BEG ||
            currentToken.getType() == LexicalUnit.IF ||
            currentToken.getType() == LexicalUnit.WHILE ||
            currentToken.getType() == LexicalUnit.PRINT ||
            currentToken.getType() == LexicalUnit.READ
        ){
            usedRules.add(4);
            ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
            parentTree.addChild(
                    instruction(instructionTree)
            );

            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instTail(instListTree)
            );
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [5] <InstTail> -> ...<InstList>
    * [6] <InstTail> -> ε
     */
    private ParseTree instTail(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.DOTS)
        {
            usedRules.add(5);
            ParseTree dotsTree = new ParseTree(currentToken);
            parentTree.addChild(dotsTree);

            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.END)
        {
            usedRules.add(6);
            return null;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
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
    private ParseTree instruction(ParseTree parentTree)
    {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME)
        {
            usedRules.add(7);
            ParseTree assignTree = new ParseTree(new Symbol(LexicalUnit.ASSIGN));
            parentTree.addChild(
                    assign(assignTree)
            );
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.IF)
        {
            usedRules.add(8);
            ParseTree ifTree = new ParseTree(new Symbol(LexicalUnit.IF));
            parentTree.addChild(
                    ifRule(ifTree)
            );
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.WHILE)
        {
            usedRules.add(9);
            ParseTree whileTree = new ParseTree(new Symbol(LexicalUnit.WHILE));
            parentTree.addChild(whileTree);
            
            ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
            parentTree.addChild(
                    cond(condTree)
            );

            ParseTree doTree = new ParseTree(new Symbol(LexicalUnit.DO));
            parentTree.addChild(doTree);

            ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
            parentTree.addChild(
                    instruction(instructionTree)
            );

            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.PRINT)
        {
            usedRules.add(10);
            // Brenno: J'attends pour faire cela, je pense que c'est mieux de faire une fonction print
            // comme pour les autres règles => modifier la grammaire ou pas car cela ne modifie pas tellement
            return null;
        }
        else if (currentToken.getType() == LexicalUnit.READ)
        {
            usedRules.add(11);
            // Brenno: J'attends pour faire cela, je pense que c'est mieux de faire une fonction read
            // comme pour les autres règles => modifier la grammaire ou pas car cela ne modifie pas tellement
            return null;
        }
        else if(currentToken.getType() == LexicalUnit.BEG ){
            usedRules.add(12);
            ParseTree beginTree = new ParseTree(currentToken);
            parentTree.addChild(beginTree);

            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );

            ParseTree endTree = new ParseTree(new Symbol(LexicalUnit.END));
            parentTree.addChild(endTree);

            return parentTree;
        }
        /*
        if (currentToken.getType() == LexicalUnit.READ) {
            // c'est un bordel, mais laisse le moi quand je reviens du sport
            // Brenno: Il serait pas mieux de créer une fonction read? genre comme c'est avant dans la grammaire
            // et comme le assign?
            usedRules.add(11);
            ParseTree readTree = new ParseTree(new Symbol(LexicalUnit.READ));
            nextToken();
            printToken();
            if (currentToken.getType() == LexicalUnit.LPAREN) {
                ParseTree lparenTree = new ParseTree(currentToken);
                parentTree.addChild(lparenTree);
                nextToken();
                printToken();
                if (currentToken.getType() == LexicalUnit.VARNAME) {
                    ParseTree varNameTree = new ParseTree(currentToken);
                    parentTree.addChild(varNameTree);
                    nextToken();
                    printToken();
                    if (currentToken.getType() == LexicalUnit.RPAREN) {
                        ParseTree rparenTree = new ParseTree(currentToken);
                        parentTree.addChild(rparenTree);

                        return parentTree;
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
        }*/
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
     * [13]	<Assign>	→	[VarName] :=<ExprArith>
     *
     * First de assign: [VarName]
     */
    private ParseTree assign(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME) {
            usedRules.add(13);
            ParseTree varNameTree = new ParseTree(currentToken);
            parentTree.addChild(varNameTree);

            ParseTree equalTree = new ParseTree(new Symbol(LexicalUnit.ASSIGN));
            parentTree.addChild(equalTree);

            ParseTree exprTree = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
            parentTree.addChild(
                    expr(exprTree)
            );

            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [14] <ExprArith> ->  <Prod><ExprArithPrime>
    * First de <ExprArith> : [VarName], [Number], (, -
    */
    private ParseTree expr(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.NUMBER ||
            currentToken.getType() == LexicalUnit.LPAREN ||
            currentToken.getType() == LexicalUnit.MINUS
        ){
            usedRules.add(14);
            ParseTree prodTree = new ParseTree(new Symbol(LexicalUnit.PROD));
            parentTree.addChild(
                    prod(prodTree)
            );

            ParseTree exprArithPrimeTree = new ParseTree(new Symbol(LexicalUnit.EXPRARITPRIME));
            parentTree.addChild(
                    exprArithPrime(exprArithPrimeTree)
            );
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [15] <ExprArithPrime>  -> +<Prod><ExprArithPrime>
    * [16] <ExprArithPrime>  -> -<Prod><ExprArithPrime>
    * [17] <ExprArithPrime>  -> ε
    * */
    private ParseTree exprArithPrime(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.PLUS)
        {
            usedRules.add(15);
            ParseTree plusTree = new ParseTree(currentToken);
            parentTree.addChild(plusTree);

            ParseTree prodTree = new ParseTree(new Symbol(LexicalUnit.PROD));
            parentTree.addChild(
                    prod(prodTree)
            );

            ParseTree exprArithPrimeTree = new ParseTree(new Symbol(LexicalUnit.EXPRARITPRIME));
            parentTree.addChild(
                    exprArithPrime(exprArithPrimeTree)
            );
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.MINUS)
        {
            usedRules.add(16);
            ParseTree minusTree = new ParseTree(currentToken);
            parentTree.addChild(minusTree);

            ParseTree prodTree = new ParseTree(new Symbol(LexicalUnit.PROD));
            parentTree.addChild(
                    prod(prodTree)
            );

            ParseTree exprArithPrimeTree = new ParseTree(new Symbol(LexicalUnit.EXPRARITPRIME));
            parentTree.addChild(
                    exprArithPrime(exprArithPrimeTree)
            );
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.END ||
                currentToken.getType() == LexicalUnit.DOTS ||
                currentToken.getType() == LexicalUnit.RPAREN ||
                currentToken.getType() == LexicalUnit.ELSE
        ){
            usedRules.add(17);
            return null;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [18] <Prod> -> <Atom><ProdPrime>
    * */
    private ParseTree prod(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.NUMBER ||
            currentToken.getType() == LexicalUnit.LPAREN ||
            currentToken.getType() == LexicalUnit.MINUS
        ){
            usedRules.add(18);
            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );
            
            ParseTree prodPrimeTree = new ParseTree(new Symbol(LexicalUnit.PRODPRIME));
            parentTree.addChild(
                    prodPrime(prodPrimeTree)
            );
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [19] <ProdPrime> -> *<Atom><ProdPrime>
    * [20] <ProdPrime> -> /<Atom><ProdPrime>
    * [21] <ProdPrime> -> ε
    * */
    private ParseTree prodPrime(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.TIMES)
        {
            usedRules.add(19);
            ParseTree timesTree = new ParseTree(currentToken);
            parentTree.addChild(timesTree);

            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );

            ParseTree prodPrimeTree = new ParseTree(new Symbol(LexicalUnit.PRODPRIME));
            parentTree.addChild(
                    prodPrime(prodPrimeTree)
            );
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.DIVIDE)
        {
            usedRules.add(20);
            ParseTree divideTree = new ParseTree(currentToken);
            parentTree.addChild(divideTree);

            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );

            ParseTree prodPrimeTree = new ParseTree(new Symbol(LexicalUnit.PRODPRIME));
            parentTree.addChild(
                    prodPrime(prodPrimeTree)
            );
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.END ||
                currentToken.getType() == LexicalUnit.DOTS ||
                currentToken.getType() == LexicalUnit.RPAREN ||
                currentToken.getType() == LexicalUnit.MINUS ||
                currentToken.getType() == LexicalUnit.PLUS ||
                currentToken.getType() == LexicalUnit.ELSE
        ){
            usedRules.add(21);
            return null;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
     * [22] <Atom> -> [VarName]
     * [23] <Atom> -> [Number]
     * [24] <Atom> -> (<ExprArith>)
     * [25] <Atom> -> -<Atom>
     */
    private ParseTree atom(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME)
        {
            usedRules.add(22);
            ParseTree varNameTree = new ParseTree(currentToken);
            parentTree.addChild(varNameTree);
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.NUMBER)
        {
            usedRules.add(23);
            ParseTree numberTree = new ParseTree(currentToken);
            parentTree.addChild(numberTree);
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.LPAREN)
        {
            usedRules.add(24);
            ParseTree lparenTree = new ParseTree(currentToken);
            parentTree.addChild(lparenTree);

            ParseTree exprTree = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
            parentTree.addChild(
                    expr(exprTree)
            );

            ParseTree rparenTree = new ParseTree(new Symbol(LexicalUnit.RPAREN));
            parentTree.addChild(rparenTree);
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.MINUS)
        {
            usedRules.add(25);
            ParseTree minusTree = new ParseTree(currentToken);
            parentTree.addChild(minusTree);

            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [26] <If> -> if<Cond>then<Instruction>else<ElseTail>
    * */
    private ParseTree ifRule(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.IF)
        {
            usedRules.add(26);
            ParseTree ifTree = new ParseTree(currentToken);
            parentTree.addChild(ifTree);

            ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
            parentTree.addChild(
                    cond(condTree)
            );


            ParseTree thenTree = new ParseTree(new Symbol(LexicalUnit.THEN));
            parentTree.addChild(thenTree);

            ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
            parentTree.addChild(
                    instruction(instructionTree)
            );

            ParseTree elseTree = new ParseTree(new Symbol(LexicalUnit.ELSE));
            parentTree.addChild(elseTree);

            ParseTree elseTailTree = new ParseTree(new Symbol(LexicalUnit.ELSETAIL));
            parentTree.addChild(
                    elseTail(elseTailTree)
            );
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [27] <ElseTail> -> <Instruction>
    * [28] <ElseTail> -> ε
    * */
    private ParseTree elseTail(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.BEG ||
            currentToken.getType() == LexicalUnit.IF ||
            currentToken.getType() == LexicalUnit.WHILE ||
            currentToken.getType() == LexicalUnit.PRINT ||
            currentToken.getType() == LexicalUnit.READ
        ){
            usedRules.add(27);
            ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
            parentTree.addChild(
                    instruction(instructionTree)
            );
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.END ||
                currentToken.getType() == LexicalUnit.DOTS ||
                currentToken.getType() == LexicalUnit.ELSE
        ){
            usedRules.add(28);
            return null;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [29] <Cond> -> <And><CondPrime>
    * */
    private ParseTree cond(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.NUMBER ||
            currentToken.getType() == LexicalUnit.LPAREN ||
            currentToken.getType() == LexicalUnit.MINUS ||
            currentToken.getType() == LexicalUnit.LBRACK
        ){
            usedRules.add(29);
            ParseTree andTree = new ParseTree(new Symbol(LexicalUnit.AND));
            parentTree.addChild(
                    and(andTree)
            );

            ParseTree condPrimeTree = new ParseTree(new Symbol(LexicalUnit.CONDPRIME));
            parentTree.addChild(
                    condPrime(condPrimeTree)
            );
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [30] <CondPrime> -> or<And><CondPrime>
    * [31] <CondPrime> -> ε
    * */
    private ParseTree condPrime(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.OR)
        {
            usedRules.add(30);
            ParseTree orTree = new ParseTree(currentToken);
            parentTree.addChild(orTree);

            ParseTree andTree = new ParseTree(new Symbol(LexicalUnit.AND));
            parentTree.addChild(
                    and(andTree)
            );

            ParseTree condPrimeTree2 = new ParseTree(new Symbol(LexicalUnit.CONDPRIME));
            parentTree.addChild(
                    condPrime(condPrimeTree2)
            );
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.DO
        ){
            usedRules.add(31);
            return null;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [32] <And> -> <CondAtom><AndPrime>
    **/
    private ParseTree and(ParseTree andTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.NUMBER ||
            currentToken.getType() == LexicalUnit.LPAREN ||
            currentToken.getType() == LexicalUnit.MINUS ||
            currentToken.getType() == LexicalUnit.LBRACK
        ){
            usedRules.add(32);
            ParseTree condAtomTree = new ParseTree(new Symbol(LexicalUnit.CONDATOM));
            andTree.addChild(
                    condAtom(condAtomTree)
            );

            ParseTree andPrimeTree = new ParseTree(new Symbol(LexicalUnit.ANDPRIME));
            andTree.addChild(
                    andPrime(andPrimeTree)
            );
            return andTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [33] <AndPrime> -> and<CondAtom><AndPrime>
    * [34] <AndPrime> -> ε
    * */
    private ParseTree andPrime(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.AND)
        {
            usedRules.add(33);
            ParseTree andTree = new ParseTree(currentToken);
            parentTree.addChild(andTree);

            ParseTree condAtomTree = new ParseTree(new Symbol(LexicalUnit.CONDATOM));
            parentTree.addChild(
                    condAtom(condAtomTree)
            );

            ParseTree andPrimeTree = new ParseTree(new Symbol(LexicalUnit.ANDPRIME));
            parentTree.addChild(
                    andPrime(andPrimeTree)
            );
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.OR ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.DO
        ){
            usedRules.add(34);
            return null;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [35] <CondAtom> -> {<Cond>}
    * [36] <CondAtom> -> <ExprArith><Comp><ExprArith>
    * */
    private ParseTree condAtom(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.OR)
        {
            usedRules.add(35);
            ParseTree lbrackTree = new ParseTree(currentToken);
            parentTree.addChild(lbrackTree);

            ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
            parentTree.addChild(
                    cond(condTree)
            );

            ParseTree rbrackTree = new ParseTree(new Symbol(LexicalUnit.RBRACK));
            parentTree.addChild(rbrackTree);
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.NUMBER ||
                currentToken.getType() == LexicalUnit.LPAREN ||
                currentToken.getType() == LexicalUnit.MINUS
        ){
            usedRules.add(36);
            ParseTree exprArithTree = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
            parentTree.addChild(
                    expr(exprArithTree)
            );

            ParseTree compTree = new ParseTree(new Symbol(LexicalUnit.COMP));
            parentTree.addChild(
                    comp(compTree)
            );

            ParseTree exprArithTree2 = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
            parentTree.addChild(
                    expr(exprArithTree2)
            );
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [37] <Comp> -> =
    * [38] <Comp> -> <
    * */
    private ParseTree comp(ParseTree parentTree) {
        nextToken();
        printToken();
        if (currentToken.getType() == LexicalUnit.EQUAL)
        {
            usedRules.add(37);
            ParseTree equalTree = new ParseTree(currentToken);
            parentTree.addChild(equalTree);
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.SMALLER)
        {
            usedRules.add(38);
            ParseTree ltTree = new ParseTree(currentToken);
            parentTree.addChild(ltTree);
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    private ParseTree read(ParseTree parentTree) {
        return null;
    }


    private void syntaxError(Symbol token) {
        System.out.println("Oh no ! Syntax error on line " + token.getLine() + " column " + token.getColumn() + " : " + token.getValue());
        //System.out.println("Expected token : " + token.getType());
        System.exit(1);
    }
}