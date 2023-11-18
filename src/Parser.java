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
        System.out.println(currentToken.toString());
    }


    public Boolean program() {
        /* First de <Program> : begin */
        printToken();
        Symbol progSymbol = new Symbol(LexicalUnit.PROGRAM);
        ParseTree root = new ParseTree(progSymbol);
        switch (currentToken.getType()) {
            case BEG:
                usedRules.add(1);

                ParseTree begintree = new ParseTree(currentToken);                  root.addChild(begintree);
                ParseTree codeTree = new ParseTree(new Symbol(LexicalUnit.CODE));   root.addChild(codeTree);
                    code(codeTree); 
                ParseTree endtree = new ParseTree(new Symbol(LexicalUnit.END));     root.addChild(endtree);
                return true;
        
            default:
                syntaxError();
                return false;
        }
    }

    private void code(ParseTree tree) {
        /* First du begin : begin, read, print, while, if, [VarName], ε */
        nextToken();
        printToken();
        System.out.println("here");
        switch (currentToken.getType()) {
            case VARNAME :
                // InstList(); et ainsi de suite 
                
                break;
            case READ :
                // InstList(); et ainsi de suite 
                break;
            default:
                break;
        }
    }

    private void syntaxError() {
        System.out.println("Syntax error");
        System.exit(1);
    }
}