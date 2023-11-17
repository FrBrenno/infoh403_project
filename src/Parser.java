import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private FileReader inputFile;
    private LexicalAnalyzer lexer;
    private Symbol currentToken;
    private Symbol lookAhead;

    public Parser(String filename) throws IOException {
        try {
            inputFile = new FileReader(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lexer = new LexicalAnalyzer(inputFile);
        lookAhead = lexer.yylex();
        nextToken();
    }

    void nextToken() throws IOException {
        currentToken = lookAhead;
        lookAhead = lexer.yylex();
    }

    void printToken() {
        /*Pas super utile, juste pour que mon monkey brain capte ce qu'il se passe hahaha */
        /*AHHAHAHAHAH tkt, ça m'est aussi utile pour comprendre*/
        System.out.println(currentToken.toString());
    }
}
