import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1){
            System.out.println("Il faut passer un fichier en argument.");
            System.exit(0);
        }

        FileReader inputFile = new FileReader(args[0]);
        LexicalAnalyzer lexer = new LexicalAnalyzer(inputFile);

        // Read each token and write it in the output file
        Symbol current_token;
        do {
            current_token = lexer.yylex();
            System.out.println(current_token.toString());
        }while (current_token.getType() != LexicalUnit.EOS);

        // Save the variables and its first declaration
    }
}
