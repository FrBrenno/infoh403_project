import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Parser {
    private String filename;
    private FileReader inputFile;
    private final LexicalAnalyzer lexer;    // Lexical analyzer that will tokenize and scan through the file. We use our own lexer from Part1
    private Symbol currentToken;    // Current token
    private final ArrayList<Integer> usedRules; // List of used rules by the parse in sequence
    private Integer lastRuleIndex = 0;  // Index of the last rule used in the list of used rules. For DEBUG PURPOSES
    private LexicalUnit state = LexicalUnit.PROGRAM; // Current state (method running) of the parser. For DEBUG PURPOSES
    private ParseTree root; // Root of the parse tree


    public Parser(String filename) throws IOException {
        try {
            this.filename = filename;
            inputFile = new FileReader(filename);
            System.out.println("########## Parsing file: " + filename + " ##########");
        } catch (Exception e) {
            System.exit(2);
        }
        usedRules = new ArrayList<>();
        lexer = new LexicalAnalyzer(inputFile);
        currentToken = lexer.yylex();
    }

    /**
     * Reads the current token from the input and get the next token
     */
    public void nextToken() {
        try {
            currentToken = lexer.yylex();
            //printToken(); // For DEBUG PURPOSES
        } catch (IOException e) {
            System.exit(3);
        }
    }

    /**
     * Returns the current token
     * @return ParseTree Parse tree of the program
     */
    public ParseTree getTree() {
        return root;
    }

    /**
     * Prints the current token. For DEBUG PURPOSES
     */
    public void printToken() {
        if (usedRules.size() > 3) {
            List<Integer> subListUsedRules = usedRules.subList(lastRuleIndex, usedRules.size() - 1);
            if (!subListUsedRules.isEmpty()) {
                System.out.println(String.format("(%d, %d) ", currentToken.getLine(), currentToken.getColumn())
                        + "Current token: " + currentToken.toString() + " | Used rules: " + subListUsedRules);
            } else {
                System.out.println("Current token: " + currentToken.toString() + " | Used rules: NO RULES");
            }
            lastRuleIndex = usedRules.size() - 1;
        } else {
            System.out.println("Current token: " + currentToken.toString());
        }
    }

    /* 
    [1] <Program> -> <Code>
    First de <Program> : begin 
    */
    public ParseTree program() {
        Symbol progSymbol = new Symbol(LexicalUnit.PROGRAM);
        root = new ParseTree(progSymbol);

        usedRules.add(1);
        ParseTree beginTree = new ParseTree(currentToken);
        root.addChild(beginTree);
        match(LexicalUnit.BEG);

        ParseTree codeTree = new ParseTree(new Symbol(LexicalUnit.CODE));
        root.addChild(
                code(codeTree)
        );

        if (currentToken.getType() == LexicalUnit.END) {
            ParseTree endTree = new ParseTree(currentToken);
            root.addChild(endTree);
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.END});
            return null;
        }

        // End of the recursive descent
        System.out.println("############## Syntax analysis finished ##############");
        System.out.printf("File %s parsed successfully !%n", filename);
        return root;
    }

    /*
     * [2] <Code> -> <InstList>
     * [3] <Code> -> ε
     * First de <Code> : [VarName], begin, if, while, print, read, ε
     */
    private ParseTree code(ParseTree parentTree) {
        state = LexicalUnit.CODE;
        if (currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.BEG ||
                currentToken.getType() == LexicalUnit.IF ||
                currentToken.getType() == LexicalUnit.WHILE ||
                currentToken.getType() == LexicalUnit.PRINT ||
                currentToken.getType() == LexicalUnit.READ
        ) {
            usedRules.add(2);
            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.END) {
            usedRules.add(3);
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.BEG,
                    LexicalUnit.IF,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.READ,
                    LexicalUnit.END});
            return null;
        }
    }

    /*
     * [4] <InstList> -> <Instruction> <InstTail>
     *
     * First de <InstList> : [VarName], begin, if, while, print, read
     */
    private ParseTree instList(ParseTree parentTree) {
        state = LexicalUnit.INSTLIST;
        usedRules.add(4);
        ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
        parentTree.addChild(
                instruction(instructionTree)
        );

        ParseTree instTailTree = new ParseTree(new Symbol(LexicalUnit.INSTTAIL));
        parentTree.addChild(
                instTail(instTailTree)
        );
        return parentTree;
    }

    /*
     * [5] <InstTail> -> ...<InstList>
     * [6] <InstTail> -> ε
     *
     * First de instTail: ..., ε
     */
    private ParseTree instTail(ParseTree parentTree) {
        state = LexicalUnit.INSTTAIL;
        if (currentToken.getType() == LexicalUnit.DOTS) {
            usedRules.add(5);
            ParseTree dotsTree = new ParseTree(currentToken);
            parentTree.addChild(dotsTree);
            match(LexicalUnit.DOTS);

            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.END) {
            usedRules.add(6);
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.DOTS, LexicalUnit.END});
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
    private ParseTree instruction(ParseTree parentTree) {
        state = LexicalUnit.INST;
        if (currentToken.getType() == LexicalUnit.VARNAME) {
            usedRules.add(7);
            ParseTree assignTree = new ParseTree(new Symbol(LexicalUnit.ASSIGN));
            parentTree.addChild(
                    assign(assignTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.IF) {
            usedRules.add(8);
            ParseTree ifTree = new ParseTree(new Symbol(LexicalUnit.IF));
            parentTree.addChild(
                    ifRule(ifTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.WHILE) {
            usedRules.add(9);
            ParseTree whileTree = new ParseTree(new Symbol(LexicalUnit.WHILE));
            parentTree.addChild(
                    whileRule(whileTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.PRINT) {
            usedRules.add(10);
            ParseTree printTree = new ParseTree(new Symbol(LexicalUnit.PRINT));
            parentTree.addChild(
                    printRule(printTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.READ) {
            usedRules.add(11);
            ParseTree readTree = new ParseTree(new Symbol(LexicalUnit.READ));
            parentTree.addChild(
                    readRule(readTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.BEG) {
            usedRules.add(12);
            ParseTree beginTree = new ParseTree(new Symbol(LexicalUnit.BEG));
            parentTree.addChild(beginTree);
            match(LexicalUnit.BEG);

            ParseTree instListTree = new ParseTree(new Symbol(LexicalUnit.INSTLIST));
            parentTree.addChild(
                    instList(instListTree)
            );

            ParseTree endTree = new ParseTree(currentToken);
            parentTree.addChild(endTree);
            match(LexicalUnit.END);

            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.IF,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.READ,
                    LexicalUnit.BEG});
            return null;
        }

    }

    /*
     * [13]	<Assign>	→	[VarName] :=<ExprArith>
     *
     * First de assign: [VarName]
     */
    private ParseTree assign(ParseTree parentTree) {
        state = LexicalUnit.ASSIGN;
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
        return parentTree;
    }


    /*
     * [14] <ExprArith> ->  <Prod><ExprArithPrime>
     * First de exprArith: [VarName], [Number], (, -
     */
    private ParseTree expr(ParseTree parentTree) {
        state = LexicalUnit.EXPRARIT;
        if (currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.NUMBER ||
                currentToken.getType() == LexicalUnit.LPAREN ||
                currentToken.getType() == LexicalUnit.MINUS
        ) {
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
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER,
                    LexicalUnit.LPAREN,
                    LexicalUnit.MINUS});
            return null;
        }
    }

    /*
     * [15] <ExprArithPrime>  -> +<Prod><ExprArithPrime>
     * [16] <ExprArithPrime>  -> -<Prod><ExprArithPrime>
     * [17] <ExprArithPrime>  -> ε
     * */
    private ParseTree exprArithPrime(ParseTree parentTree) {
        state = LexicalUnit.EXPRARITPRIME;
        if (currentToken.getType() == LexicalUnit.PLUS) {
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
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.MINUS) {
            usedRules.add(16);
            ParseTree minusTree = new ParseTree(currentToken);
            parentTree.addChild(minusTree);
            match(LexicalUnit.MINUS);

            ParseTree prodTree = new ParseTree(new Symbol(LexicalUnit.PROD));
            parentTree.addChild(
                    prod(prodTree)
            );

            ParseTree exprArithPrimeTree = new ParseTree(new Symbol(LexicalUnit.EXPRARITPRIME));
            parentTree.addChild(
                    exprArithPrime(exprArithPrimeTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.END ||
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
        ) {
            usedRules.add(17);
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.PLUS,
                    LexicalUnit.MINUS,
                    LexicalUnit.END,
                    LexicalUnit.DOTS,
                    LexicalUnit.RPAREN,
                    LexicalUnit.ELSE,
                    LexicalUnit.THEN,
                    LexicalUnit.OR,
                    LexicalUnit.RBRACK,
                    LexicalUnit.DO,
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER,
                    LexicalUnit.AND});
            return null;
        }
    }

    /*
     * [18] <Prod> -> <Atom><ProdPrime>
     * */
    private ParseTree prod(ParseTree parentTree) {
        state = LexicalUnit.PROD;
        if (currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.NUMBER ||
                currentToken.getType() == LexicalUnit.LPAREN ||
                currentToken.getType() == LexicalUnit.MINUS
        ) {
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
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER,
                    LexicalUnit.LPAREN,
                    LexicalUnit.MINUS});
            return null;
        }
    }

    /*
     * [19] <ProdPrime> -> *<Atom><ProdPrime>
     * [20] <ProdPrime> -> /<Atom><ProdPrime>
     * [21] <ProdPrime> -> ε
     * */
    private ParseTree prodPrime(ParseTree parentTree) {
        state = LexicalUnit.PRODPRIME;
        if (currentToken.getType() == LexicalUnit.TIMES) {
            usedRules.add(19);
            ParseTree timesTree = new ParseTree(currentToken);
            parentTree.addChild(timesTree);
            match(LexicalUnit.TIMES);

            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );

            ParseTree prodPrimeTree = new ParseTree(new Symbol(LexicalUnit.PRODPRIME));
            parentTree.addChild(
                    prodPrime(prodPrimeTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.DIVIDE) {
            usedRules.add(20);
            ParseTree divideTree = new ParseTree(currentToken);
            parentTree.addChild(divideTree);
            match(LexicalUnit.DIVIDE);

            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );

            ParseTree prodPrimeTree = new ParseTree(new Symbol(LexicalUnit.PRODPRIME));
            parentTree.addChild(
                    prodPrime(prodPrimeTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.END ||
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
        ) {
            usedRules.add(21);
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.TIMES,
                    LexicalUnit.DIVIDE,
                    LexicalUnit.END,
                    LexicalUnit.DOTS,
                    LexicalUnit.RPAREN,
                    LexicalUnit.MINUS,
                    LexicalUnit.PLUS,
                    LexicalUnit.THEN,
                    LexicalUnit.ELSE,
                    LexicalUnit.OR,
                    LexicalUnit.AND,
                    LexicalUnit.RBRACK,
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER,
                    LexicalUnit.DO});
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
        state = LexicalUnit.ATOM;
        if (currentToken.getType() == LexicalUnit.VARNAME) {
            usedRules.add(22);
            ParseTree varNameTree = new ParseTree(currentToken);
            parentTree.addChild(varNameTree);
            match(LexicalUnit.VARNAME);
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.NUMBER) {
            usedRules.add(23);
            ParseTree numberTree = new ParseTree(currentToken);
            parentTree.addChild(numberTree);
            match(LexicalUnit.NUMBER);

            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.LPAREN) {
            usedRules.add(24);
            ParseTree lparenTree = new ParseTree(currentToken);
            parentTree.addChild(lparenTree);
            match(LexicalUnit.LPAREN);

            ParseTree exprTree = new ParseTree(new Symbol(LexicalUnit.EXPRARIT));
            parentTree.addChild(
                    expr(exprTree)
            );

            if (currentToken.getType() == LexicalUnit.RPAREN) {
                ParseTree rparenTree = new ParseTree(currentToken);
                parentTree.addChild(rparenTree);
                match(LexicalUnit.RPAREN);
                return parentTree;
            } else {
                syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.RPAREN});
                return null;
            }
        } else if (currentToken.getType() == LexicalUnit.MINUS) {
            usedRules.add(25);
            ParseTree minusTree = new ParseTree(currentToken);
            parentTree.addChild(minusTree);
            match(LexicalUnit.MINUS);

            ParseTree atomTree = new ParseTree(new Symbol(LexicalUnit.ATOM));
            parentTree.addChild(
                    atom(atomTree)
            );
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER,
                    LexicalUnit.LPAREN,
                    LexicalUnit.MINUS});
            return null;
        }
    }

    /*
     * [26] <If> -> if<Cond>then<Instruction>else<ElseTail>
     * */
    private ParseTree ifRule(ParseTree parentTree) {
        state = LexicalUnit.IF;
        usedRules.add(26);
        ParseTree ifTree = new ParseTree(currentToken);
        parentTree.addChild(ifTree);
        match(LexicalUnit.IF);

        ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
        parentTree.addChild(
                cond(condTree)
        );

        ParseTree thenTree = new ParseTree(currentToken);
        parentTree.addChild(thenTree);
        match(LexicalUnit.THEN);

        ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
        parentTree.addChild(
                instruction(instructionTree)
        );

        ParseTree elseTree = new ParseTree(currentToken);
        parentTree.addChild(elseTree);
        match(LexicalUnit.ELSE);

        ParseTree elseTailTree = new ParseTree(new Symbol(LexicalUnit.ELSETAIL));
        parentTree.addChild(
                elseTail(elseTailTree)
        );
        return parentTree;


    }

    /*
     * [27] <ElseTail> -> <Instruction>
     * [28] <ElseTail> -> ε
     * */
    private ParseTree elseTail(ParseTree parentTree) {
        state = LexicalUnit.ELSETAIL;
        if (currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.BEG ||
                currentToken.getType() == LexicalUnit.IF ||
                currentToken.getType() == LexicalUnit.WHILE ||
                currentToken.getType() == LexicalUnit.PRINT ||
                currentToken.getType() == LexicalUnit.READ
        ) {
            usedRules.add(27);
            ParseTree instructionTree = new ParseTree(new Symbol(LexicalUnit.INST));
            parentTree.addChild(
                    instruction(instructionTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.END ||
                currentToken.getType() == LexicalUnit.DOTS ||
                currentToken.getType() == LexicalUnit.ELSE
        ) {
            usedRules.add(28);
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.BEG,
                    LexicalUnit.IF,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.READ,
                    LexicalUnit.END,
                    LexicalUnit.DOTS,
                    LexicalUnit.ELSE});
            return null;
        }
    }

    /*
     * [29] <Cond> -> <And><CondPrime>
     * */
    private ParseTree cond(ParseTree parentTree) {
        state = LexicalUnit.COND;
        if (currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.NUMBER ||
                currentToken.getType() == LexicalUnit.LPAREN ||
                currentToken.getType() == LexicalUnit.MINUS ||
                currentToken.getType() == LexicalUnit.LBRACK
        ) {
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
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER,
                    LexicalUnit.LPAREN,
                    LexicalUnit.MINUS,
                    LexicalUnit.LBRACK});
            return null;
        }
    }

    /*
     * [30] <CondPrime> -> or<And><CondPrime>
     * [31] <CondPrime> -> ε
     * */
    private ParseTree condPrime(ParseTree parentTree) {
        state = LexicalUnit.CONDPRIME;
        if (currentToken.getType() == LexicalUnit.OR) {
            usedRules.add(30);
            ParseTree orTree = new ParseTree(currentToken);
            parentTree.addChild(orTree);
            match(LexicalUnit.OR);

            ParseTree andTree = new ParseTree(new Symbol(LexicalUnit.AND));
            parentTree.addChild(
                    and(andTree)
            );

            ParseTree condPrimeTree = new ParseTree(new Symbol(LexicalUnit.CONDPRIME));
            parentTree.addChild(
                    condPrime(condPrimeTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.DO
        ) {
            usedRules.add(31);
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.OR,
                    LexicalUnit.THEN,
                    LexicalUnit.RBRACK,
                    LexicalUnit.DO});
            return null;
        }
    }

    /*
     * [32] <And> -> <CondAtom><AndPrime>
     **/
    private ParseTree and(ParseTree parentTree) {
        state = LexicalUnit.AND;
        if (currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.NUMBER ||
                currentToken.getType() == LexicalUnit.LPAREN ||
                currentToken.getType() == LexicalUnit.MINUS ||
                currentToken.getType() == LexicalUnit.LBRACK
        ) {
            usedRules.add(32);
            ParseTree condAtomTree = new ParseTree(new Symbol(LexicalUnit.CONDATOM));
            parentTree.addChild(
                    condAtom(condAtomTree)
            );

            ParseTree andPrimeTree = new ParseTree(new Symbol(LexicalUnit.ANDPRIME));
            parentTree.addChild(
                    andPrime(andPrimeTree)
            );
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER,
                    LexicalUnit.LPAREN,
                    LexicalUnit.MINUS,
                    LexicalUnit.LBRACK});
            return null;
        }
    }

    /*
     * [33] <AndPrime> -> and<CondAtom><AndPrime>
     * [34] <AndPrime> -> ε
     * */
    private ParseTree andPrime(ParseTree parentTree) {
        state = LexicalUnit.ANDPRIME;
        if (currentToken.getType() == LexicalUnit.AND) {
            usedRules.add(33);
            ParseTree andTree = new ParseTree(currentToken);
            parentTree.addChild(andTree);
            match(LexicalUnit.AND);

            ParseTree condAtomTree = new ParseTree(new Symbol(LexicalUnit.CONDATOM));
            parentTree.addChild(
                    condAtom(condAtomTree)
            );

            ParseTree andPrimeTree = new ParseTree(new Symbol(LexicalUnit.ANDPRIME));
            parentTree.addChild(
                    andPrime(andPrimeTree)
            );
            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.THEN ||
                currentToken.getType() == LexicalUnit.OR ||
                currentToken.getType() == LexicalUnit.RBRACK ||
                currentToken.getType() == LexicalUnit.DO
        ) {
            usedRules.add(34);
            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.AND,
                    LexicalUnit.THEN,
                    LexicalUnit.OR,
                    LexicalUnit.RBRACK,
                    LexicalUnit.DO});
            return null;
        }
    }

    /*
     * [35] <CondAtom> -> {<Cond>}
     * [36] <CondAtom> -> <ExprArith><Comp><ExprArith>
     * */
    private ParseTree condAtom(ParseTree parentTree) {
        state = LexicalUnit.CONDATOM;
        if (currentToken.getType() == LexicalUnit.LBRACK) {
            usedRules.add(35);
            ParseTree lbrackTree = new ParseTree(currentToken);
            parentTree.addChild(lbrackTree);
            match(LexicalUnit.LBRACK);

            ParseTree condTree = new ParseTree(new Symbol(LexicalUnit.COND));
            parentTree.addChild(
                    cond(condTree)
            );

            ParseTree rbrackTree = new ParseTree(currentToken);
            parentTree.addChild(rbrackTree);
            match(LexicalUnit.RBRACK);

            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.VARNAME ||
                currentToken.getType() == LexicalUnit.NUMBER ||
                currentToken.getType() == LexicalUnit.LPAREN ||
                currentToken.getType() == LexicalUnit.MINUS
        ) {
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
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.LBRACK,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER,
                    LexicalUnit.LPAREN,
                    LexicalUnit.MINUS});
            return null;
        }
    }

    /*
     * [37] <Comp> -> =
     * [38]        -> <
     * */
    private ParseTree comp(ParseTree parentTree) {
        state = LexicalUnit.COMP;
        if (currentToken.getType() == LexicalUnit.EQUAL) {
            usedRules.add(37);
            ParseTree equalTree = new ParseTree(currentToken);
            parentTree.addChild(equalTree);
            match(LexicalUnit.EQUAL);

            return parentTree;
        } else if (currentToken.getType() == LexicalUnit.SMALLER) {
            usedRules.add(38);
            ParseTree smallerTree = new ParseTree(currentToken);
            parentTree.addChild(smallerTree);
            match(LexicalUnit.SMALLER);

            return parentTree;
        } else {
            syntaxError(currentToken, new LexicalUnit[]{LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER});
            return null;
        }
    }

    /*
     * [39] <While> -> while<Cond>do<Instruction>
     * */
    private ParseTree whileRule(ParseTree parentTree) {
        state = LexicalUnit.WHILE;
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
        return parentTree;
    }


    /*
     * [40] <Print> -> print(<VarName>)
     * */
    private ParseTree printRule(ParseTree parentTree) {
        state = LexicalUnit.PRINT;
        usedRules.add(40);
        ParseTree printTree = new ParseTree(currentToken);
        parentTree.addChild(printTree);
        match(LexicalUnit.PRINT);


        ParseTree lparenPrintTree = new ParseTree(currentToken);
        parentTree.addChild(lparenPrintTree);
        match(LexicalUnit.LPAREN);

        ParseTree varNameTree = new ParseTree(currentToken);
        parentTree.addChild(varNameTree);
        match(LexicalUnit.VARNAME);

        ParseTree rparenTree = new ParseTree(currentToken);
        parentTree.addChild(rparenTree);
        match(LexicalUnit.RPAREN);

        return parentTree;
    }


    /*
     * [41] <Read> -> read(<VarName>)
     * */
    private ParseTree readRule(ParseTree parentTree) {
        state = LexicalUnit.READ;
        usedRules.add(41);
        ParseTree readTree = new ParseTree(currentToken);
        parentTree.addChild(readTree);
        match(LexicalUnit.READ);

        ParseTree lparenReadTree = new ParseTree(currentToken);
        parentTree.addChild(lparenReadTree);
        match(LexicalUnit.LPAREN);

        ParseTree varNameTree = new ParseTree(currentToken);
        parentTree.addChild(varNameTree);
        match(LexicalUnit.VARNAME);

        ParseTree rparenTree = new ParseTree(currentToken);
        parentTree.addChild(rparenTree);
        match(LexicalUnit.RPAREN);

        return parentTree;
    }

    /**
     * Matches the current token with the expected one. It reads the matched token from the input and get the next one.
     * @param expected Expected token to match
     */
    private void match(LexicalUnit expected) {
        if (currentToken.getType() == expected) {
            nextToken(); // Consume the current token if there's a match
        } else {
            System.out.println("############## Match error ##############");
            System.out.println("The current token is: " + currentToken.toString() + " but the expected one was: " + expected.toString());
            syntaxError(currentToken, new LexicalUnit[]{expected});
        }
    }

    /**
     * Prints the syntax error and exits the program
     * @param token Current token
     * @param expected Expected tokens
     */
    private void syntaxError(Symbol token, LexicalUnit[] expected) {
        System.out.println("############## Syntax error ##############");
        System.out.printf("Oh no ! Syntax error on line %d column %d : %s [%s]%n",
                token.getLine(),
                token.getColumn(),
                token.getValue(),
                state.toString()
        );
        System.out.println("Expected tokens: " + Arrays.toString(expected));
        System.out.println("Used Rules: " + usedRules);
        System.exit(1);
    }
}