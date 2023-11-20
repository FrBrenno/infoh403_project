import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    Integer i = 0;
    private FileReader inputFile;
    private LexicalAnalyzer lexer;
    private Symbol currentToken;
    private ArrayList<Integer> usedRules;
    private Integer lastRuleIndex = 0;
    

    public Parser(String filename) throws IOException {
        try {
            inputFile = new FileReader(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        usedRules = new ArrayList<Integer>();
        lexer = new LexicalAnalyzer(inputFile);
        currentToken = lexer.yylex();
    }

    public Symbol getCurrentToken() {
        return currentToken;
    }

    public void nextToken() {

        try {
            currentToken = lexer.yylex();
            printToken();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printToken() {
        if (usedRules.size() > 3)
        {
            List<Integer> subListUsedRules = usedRules.subList(lastRuleIndex, usedRules.size()-1);
            if (!subListUsedRules.isEmpty()) {
                System.out.println("Current token: " + currentToken.toString() + " | Used rules: " + subListUsedRules);
            }
            else
            {
                System.out.println("Current token: " + currentToken.toString() + " | Used rules: NO RULES" );
            }
            lastRuleIndex = usedRules.size() - 1;
        }
        else
        {
            System.out.println("Current token: " + currentToken.toString());
        }
    }

    /* 
    [1] <Program> -> <Code>
    First de <Program> : begin 
    */
    public ParseTree program() {
        Symbol progSymbol = new Symbol(LexicalUnit.PROGRAM);
        ParseTree root = new ParseTree(progSymbol);

        usedRules.add(1);
        ParseTree beginTree = new ParseTree(currentToken);
        root.addChild(beginTree);
        match(LexicalUnit.BEG);

        ParseTree codeTree = new ParseTree(new Symbol(LexicalUnit.CODE));
        root.addChild(
                code(codeTree)
        );

        if(currentToken.getType() == LexicalUnit.END){
            ParseTree endTree = new ParseTree(currentToken);
            root.addChild(endTree);
        }
        else {
            syntaxError(currentToken);
            return null;
        }
        System.out.println("############## Syntax analysis finished ##############");
        System.out.println("Sequence des règles utilisée : \n" + usedRules);
        System.out.println("Arbre de dérivation en Latex: \n");
        System.out.println(root.toLaTeX());
        return root;
    }

    /*
     * [2] <Code> -> <InstList>
     * [3] <Code> -> ε
     * First de <Code> : [VarName], begin, if, while, print, read, ε
     */
    private ParseTree code(ParseTree parentTree) {
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
            return parentTree ;
        }
        else if (currentToken.getType() == LexicalUnit.END) {
            usedRules.add(3);
            return parentTree ;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
     * [4] <InstList> -> <Instruction> <InstTail>
     * 
     * First de <InstList> : [VarName], begin, if, while, print, read
     */
    private ParseTree instList(ParseTree parentTree) {
        System.out.println("Entré dans instlist");

        usedRules.add(4);
        ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
        parentTree.addChild(
                instruction(instructionTree)
        );

        ParseTree instTailTree = new ParseTree(new Symbol(LexicalUnit.INSTTAIL));
        parentTree.addChild(
                instTail(instTailTree)
        );
        System.out.println("Sorti d'un instlist");
        return parentTree;
    }

    /*
    * [5] <InstTail> -> ...<InstList>
    * [6] <InstTail> -> ε
    *
    * First de instTail: ..., ε
    */
    private ParseTree instTail(ParseTree parentTree) {
        System.out.println("Entré dans instTail");
        if (currentToken.getType() == LexicalUnit.DOTS)
        {
            usedRules.add(5);
            ParseTree dotsTree = new ParseTree(currentToken);
            parentTree.addChild(dotsTree);
            match(LexicalUnit.DOTS);

            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );
            System.out.println("insttail fini");
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.END)
        {
            usedRules.add(6);
            System.out.println("insttail fini");
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    [7] <Instruction>   →	<Assign>
	[8]                 →	<If>
	[9]                 →	<While>
	[10]                →	<Print>
	[11]                →	<Read>
	[12]                →	begin<InstList>end

    * First de inst: begin, read, print, while, if, [VarName]
     */
    private ParseTree instruction(ParseTree parentTree)
    {
        System.out.println("Entré dans instruction");
        
        if (currentToken.getType() == LexicalUnit.VARNAME)
        {
            usedRules.add(7);
            ParseTree assignTree = new ParseTree(new Symbol(LexicalUnit.ASSIGN));
            parentTree.addChild(
                    assign(assignTree)
            );
            System.out.println("instruction fini");
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.IF)
        {
            usedRules.add(8);
            ParseTree ifTree = new ParseTree(new Symbol(LexicalUnit.IF));
            parentTree.addChild(
                    ifRule(ifTree)
            );
            System.out.println("instruction fini");
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.WHILE)
        {
            usedRules.add(9);
            ParseTree whileTree = new ParseTree(new Symbol(LexicalUnit.WHILE));
            parentTree.addChild(
                    whileRule(whileTree)
            );
            System.out.println("instruction fini");
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.PRINT)
        {
            usedRules.add(10);
            ParseTree printTree = new ParseTree(new Symbol(LexicalUnit.PRINT));
            parentTree.addChild(
                    printRule(printTree)
            );
            System.out.println("instruction fini");
            return parentTree;
        }
        else if (currentToken.getType() == LexicalUnit.READ)
        {
            usedRules.add(11);
            ParseTree readTree = new ParseTree(new Symbol(LexicalUnit.READ));
            parentTree.addChild(
                    readRule(readTree)
            );
            System.out.println("instruction fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.BEG ){
            usedRules.add(12);
            ParseTree beginTree = new ParseTree(new Symbol(LexicalUnit.BEG));
            parentTree.addChild(beginTree);
            match(LexicalUnit.BEG);
            
            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );

            
            if(currentToken.getType() == LexicalUnit.END){
                ParseTree endTree = new ParseTree(new Symbol(LexicalUnit.END));
                parentTree.addChild(endTree);
            }
            else {
                syntaxError(currentToken);
                return null;
            }
            System.out.println("instruction fini");
            return parentTree;
        }
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
        System.out.println("Entré dans assign");

        usedRules.add(13);
        ParseTree varNameTree = new ParseTree(currentToken);
        parentTree.addChild(varNameTree);
        match(LexicalUnit.VARNAME);

        ParseTree assignTree = new ParseTree(currentToken);
        parentTree.addChild(assignTree);
        match(LexicalUnit.ASSIGN);
        
        ParseTree exprTree = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
        parentTree.addChild(
                expr(exprTree)
        );
        System.out.println("assign fini");
        return parentTree;
    }


    /*
    * [14] <ExprArith> ->  <Prod><ExprArithPrime>
    * First de exprArith: [VarName], [Number], (, -
    */
    private ParseTree expr(ParseTree parentTree) {
        System.out.println("Entré dans expr");
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
            System.out.println("expr fini");
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
        System.out.println("Entré dans exprArithPrime");
        if (currentToken.getType() == LexicalUnit.PLUS)
        {
            usedRules.add(15);
            ParseTree plusTree = new ParseTree(currentToken);
            parentTree.addChild(plusTree);
            match(LexicalUnit.PLUS);
            
            ParseTree prodTree = new ParseTree(new Symbol(LexicalUnit.PROD));
            parentTree.addChild(
                    prod(prodTree)
            );

            ParseTree exprArithPrimeTree = new ParseTree(new Symbol(LexicalUnit.EXPRARITPRIME));
            parentTree.addChild(
                    exprArithPrime(exprArithPrimeTree)
            );
            System.out.println("exprArithPrime fini");
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
            System.out.println("exprArithPrime fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.END ||
                currentToken.getType() == LexicalUnit.DOTS ||
                currentToken.getType() == LexicalUnit.RPAREN ||
                currentToken.getType() == LexicalUnit.ELSE ||
                currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.OR ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.DO ||
                currentToken.getType() == LexicalUnit.EQUAL ||
                currentToken.getType() == LexicalUnit.SMALLER ||
                currentToken.getType() == LexicalUnit.AND 
        ){
            usedRules.add(17);
            System.out.println("exprArithPrime fini");
            return parentTree;
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
        System.out.println("Entré dans prod");
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
            System.out.println("prodprime fini");
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
        System.out.println("Entré dans prodPrime");
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
            System.out.println("prodPrime fini");
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
            System.out.println("prodPrime fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.END ||
                currentToken.getType() == LexicalUnit.DOTS ||
                currentToken.getType() == LexicalUnit.RPAREN ||
                currentToken.getType() == LexicalUnit.MINUS ||
                currentToken.getType() == LexicalUnit.PLUS ||
                currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.ELSE ||
                currentToken.getType() == LexicalUnit.OR ||
                currentToken.getType() == LexicalUnit.AND ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.EQUAL ||
                currentToken.getType() == LexicalUnit.SMALLER ||
                currentToken.getType() == LexicalUnit.DO
        ){
            usedRules.add(21);
            System.out.println("prodPrime fini");
            return parentTree;
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
        System.out.println("Entré dans atom");
        if (currentToken.getType() == LexicalUnit.VARNAME)
        {
            usedRules.add(22);
            ParseTree varNameTree = new ParseTree(currentToken);
            parentTree.addChild(varNameTree);
            match(LexicalUnit.VARNAME);
            System.out.println("atom fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.NUMBER)
        {
            usedRules.add(23);
            ParseTree numberTree = new ParseTree(currentToken);
            parentTree.addChild(numberTree);
            match(LexicalUnit.NUMBER);
            System.out.println("atom fini");
            
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.LPAREN)
        {
            usedRules.add(24);
            ParseTree lparenTree = new ParseTree(currentToken);
            parentTree.addChild(lparenTree);
            match(LexicalUnit.LPAREN);

            ParseTree exprTree = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
            parentTree.addChild(
                    expr(exprTree)
            );

            if(currentToken.getType() == LexicalUnit.RPAREN)
            {
                ParseTree rparenTree = new ParseTree(currentToken);
                parentTree.addChild(rparenTree);
                match(LexicalUnit.RPAREN);
                System.out.println("atom fini");
                return parentTree;
            }
            else {
                syntaxError(currentToken);
                return null;
            }
        }
        else if(currentToken.getType() == LexicalUnit.MINUS)
        {
            usedRules.add(25);
            ParseTree minusTree = new ParseTree(currentToken);
            parentTree.addChild(minusTree);
            match(LexicalUnit.MINUS);

            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );
            System.out.println("atom fini");
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
        System.out.println("Entré dans if");
        usedRules.add(26);
        ParseTree ifTree = new ParseTree(currentToken);
        parentTree.addChild(ifTree);
    

        ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
        parentTree.addChild(
                cond(condTree)
        );

        if(currentToken.getType()==LexicalUnit.THEN){
            ParseTree thenTree = new ParseTree(currentToken);
            parentTree.addChild(thenTree);}
        else {
            syntaxError(currentToken);
            return null;
        }

        ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
        parentTree.addChild(
                instruction(instructionTree)
        );

        
        if(currentToken.getType() == LexicalUnit.ELSE)
        {
            ParseTree elseTree = new ParseTree(currentToken);
            parentTree.addChild(elseTree);
        }
        else {
            syntaxError(currentToken);
            return null;
        }

        ParseTree elseTailTree = new ParseTree(new Symbol(LexicalUnit.ELSETAIL));
        parentTree.addChild(
                elseTail(elseTailTree)
        );
        System.out.println("if fini");
        return parentTree;
            
        
    }

    /*
    * [27] <ElseTail> -> <Instruction>
    * [28] <ElseTail> -> ε
    * */
    private ParseTree elseTail(ParseTree parentTree) {
        System.out.println("Entré dans elseTail");
        
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
            System.out.println("elseTail fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.END ||
                currentToken.getType() == LexicalUnit.DOTS ||
                currentToken.getType() == LexicalUnit.ELSE
        ){
            usedRules.add(28);
            System.out.println("elseTail fini");
            return parentTree;
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
        System.out.println("Entré dans cond");
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
            System.out.println("cond fini");
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
        System.out.println("Entré dans condPrime");
        if (currentToken.getType() == LexicalUnit.OR)
        {
            usedRules.add(30);
            ParseTree orTree = new ParseTree(currentToken);
            parentTree.addChild(orTree);

            ParseTree andTree = new ParseTree(new Symbol(LexicalUnit.AND));
            parentTree.addChild(
                    and(andTree)
            );

            ParseTree condPrimeTree = new ParseTree(new Symbol(LexicalUnit.CONDPRIME));
            parentTree.addChild(
                    condPrime(condPrimeTree)
            );
            System.out.println("condPrime fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.DO
        ){
            usedRules.add(31);
            System.out.println("condPrime fini");
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [32] <And> -> <CondAtom><AndPrime>
    **/
    private ParseTree and(ParseTree parentTree) {
        System.out.println("Entré dans and");
        if (currentToken.getType() == LexicalUnit.VARNAME ||
            currentToken.getType() == LexicalUnit.NUMBER ||
            currentToken.getType() == LexicalUnit.LPAREN ||
            currentToken.getType() == LexicalUnit.MINUS ||
            currentToken.getType() == LexicalUnit.LBRACK
        ){
            usedRules.add(32);
            ParseTree condAtomTree = new ParseTree(new Symbol(LexicalUnit.CONDATOM));
            parentTree.addChild(
                    condAtom(condAtomTree)
            );

            ParseTree andPrimeTree = new ParseTree(new Symbol(LexicalUnit.ANDPRIME));
            parentTree.addChild(
                    andPrime(andPrimeTree)
            );
            System.out.println("and fini");
            return parentTree;
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
        System.out.println("Entré dans andPrime");
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
            System.out.println("andPrime fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.OR ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.DO
        ){
            usedRules.add(34);
            System.out.println("andPrime fini");
            return parentTree;
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
        System.out.println("Entré dans condAtom");
        if (currentToken.getType() == LexicalUnit.LBRACK)
        {
            usedRules.add(35);
            ParseTree lbrackTree = new ParseTree(currentToken);
            parentTree.addChild(lbrackTree);

            ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
            parentTree.addChild(
                    cond(condTree)
            );

            if(currentToken.getType() == LexicalUnit.RBRACK){
                ParseTree rbrackTree = new ParseTree(currentToken);
                parentTree.addChild(rbrackTree);
            }
            else {
                syntaxError(currentToken);
                return null;
            }
            System.out.println("condAtom fini");
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
            System.out.println("condAtom fini");
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [37] <Comp> -> =
    * [38]        -> <
    * */
    private ParseTree comp(ParseTree parentTree) {
        System.out.println("Entré dans comp");
        if (currentToken.getType() == LexicalUnit.EQUAL)
        {
            usedRules.add(37);
            ParseTree equalTree = new ParseTree(currentToken);
            parentTree.addChild(equalTree);
            match(LexicalUnit.EQUAL);
            System.out.println("comp fini");
            return parentTree;
        }
        else if(currentToken.getType() == LexicalUnit.SMALLER)
        {
            usedRules.add(38);
            ParseTree smallerTree = new ParseTree(currentToken);
            parentTree.addChild(smallerTree);
            match(LexicalUnit.SMALLER);
            System.out.println("comp fini");
            return parentTree;
        }
        else {
            syntaxError(currentToken);
            return null;
        }
    }

    /*
    * [39] <While> -> while<Cond>do<Instruction>
    * */
    private ParseTree whileRule(ParseTree parentTree) {
        System.out.println("Entré dans whileRule");

        usedRules.add(39);
        ParseTree whileTree = new ParseTree(currentToken);
        parentTree.addChild(whileTree);
        match(LexicalUnit.WHILE);

        ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
        parentTree.addChild(
                cond(condTree)
                );

        ParseTree doTree = new ParseTree(currentToken);
        parentTree.addChild(doTree);
        match(LexicalUnit.DO);

        ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
        parentTree.addChild(
                instruction(instructionTree)
        );
        System.out.println("whileRule fini");
        return parentTree;
    }
    

    /*
    * [40] <Print> -> print(<VarName>)
    * */
    private ParseTree printRule(ParseTree parentTree) {
        System.out.println("Entré dans printRule");
        usedRules.add(40);
        ParseTree printTree = new ParseTree(currentToken);
        parentTree.addChild(printTree);

        
        if (currentToken.getType() == LexicalUnit.LPAREN)
        {
            ParseTree lparenTree = new ParseTree(currentToken);
            parentTree.addChild(lparenTree);
        }
        else
        {
            syntaxError(currentToken);
            return null;
        }
        
        
        if (currentToken.getType() == LexicalUnit.VARNAME)
        {
            ParseTree varNameTree = new ParseTree(currentToken);
            parentTree.addChild(varNameTree);
        }
        else
        {
            syntaxError(currentToken);
            return null;
        }

        
        if (currentToken.getType() == LexicalUnit.RPAREN)
        {
            ParseTree rparenTree = new ParseTree(currentToken);
            parentTree.addChild(rparenTree);
        }
        else
        {
            syntaxError(currentToken);
            return null;
        }
        System.out.println("printRule fini");
        return parentTree;
    }
    

    /*
    * [41] <Read> -> read(<VarName>)
    * */
    private ParseTree readRule(ParseTree parentTree) {
        System.out.println("Entré dans readRule");

        usedRules.add(41);
        ParseTree readTree = new ParseTree(currentToken);
        parentTree.addChild(readTree);
        match(LexicalUnit.READ);

        ParseTree lparenTree = new ParseTree(currentToken);
        parentTree.addChild(lparenTree);
        match(LexicalUnit.LPAREN);

        ParseTree varNameTree = new ParseTree(currentToken);
        parentTree.addChild(varNameTree);
        match(LexicalUnit.VARNAME);

        ParseTree rparenTree = new ParseTree(currentToken);
        parentTree.addChild(rparenTree);
        match(LexicalUnit.RPAREN);

        System.out.println("readRule fini");
        return parentTree;
    }

    private void match(LexicalUnit expected) {
        if (currentToken.getType() == expected) {
            System.out.println("Token Matched: " + currentToken.toString());
            nextToken(); // Consume the current token if there's a match
        } else {
            System.out.println("############## Match error ##############");
            System.out.println("The current token is: "+ currentToken.toString() + " but the expected one was: " + expected.toString());
            syntaxError(currentToken);
        }
    }


    private void syntaxError(Symbol token) {
        System.out.println("############## Syntax error ##############");
        System.out.println("Oh no ! Syntax error on line " + token.getLine() + " column " + token.getColumn() + " : " + token.getValue());
        System.out.println("Entré dans la règle : " + usedRules);
        System.out.println("Progression: " + token.getLine() +token.getColumn());
        System.exit(1);
    }
}