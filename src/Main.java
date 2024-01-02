import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Main class
 */
public class Main {
    /**
     * Main method that runs the LexicalAnalyzer.
     *
     * @param args the path to the input file
     * @throws IOException Not able to find or open the file
     */
    public static void main(String[] args) throws IOException {
        String filepath;

        // Verify arguments entry
        if (args.length < 1) {
            System.out.println("Il faut passer un fichier en argument.");
            System.exit(1);
        }
        else if (args.length == 1) { // Command: java -jar ./dist/part2.jar <inputFile>
            // Get arguments
            filepath = args[0];
            // Parse
            Parser parser = new Parser(filepath);
            parser.program();
            ParseTree parseTree = parser.getParseTree();
            // Generate AST
            ASTGenerator astGenerator = new ASTGenerator(parseTree);
            ParseTree ast = astGenerator.generateAST();
            // Generate Code
            LLVMGenerator LLVMGenerator = new LLVMGenerator(ast);
            LLVMGenerator.generate();
            // Write code into standard output
            System.out.println(LLVMGenerator.getCode().toString());
        }
        else {
            System.out.println("Invalid command. See usage:");
            System.out.println("Usage: java -jar ./dist/part2.jar <inputFile>");
            System.exit(1);
        }
        System.exit(0);
    }
}
