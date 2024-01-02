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
        String latexFilepath;
        String llvmFilepath;

        // Verify arguments entry
        if (args.length < 1) {
            System.out.println("Il faut passer un fichier en argument.");
            System.exit(1);
        }
        else if (args.length == 1) { // Command: java -jar ./dist/part2.jar <inputFile>
            // Get arguments
            filepath = args[0];
            llvmFilepath = filepath.replace(".pmp", ".ll");
            llvmFilepath = llvmFilepath.replace("/test/", "/test/out/");
            // Parse
            ParseTree parseTree = parse(filepath);
            // Generate AST
            ASTGenerator astGenerator = new ASTGenerator(parseTree);
            ParseTree ast = astGenerator.generateAST();
            LLVMGenerator LLVMGenerator = new LLVMGenerator();
            LLVMGenerator.generate(ast);
            
            // Write to latex file

            try{
                File llvmFile = new File(llvmFilepath);
                FileWriter llvmFileWriter = new FileWriter(llvmFile);
                llvmFileWriter.write(LLVMGenerator.getCode().toString());
                llvmFileWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred while writing the latex.");
                System.exit(1);
            }
        }
        else if (args.length == 3 && "-wt".equals(args[0])) // Command: java -jar ./dist/part2.jar -wt <latexFile> <inputFile>
        {
            // Get arguments
            latexFilepath = args[1];
            llvmFilepath = latexFilepath.replace(".tex", ".ll");
            
            filepath = args[2];
            // Parse
            ParseTree parseTree = parse(filepath);
            // Generate AST
            ASTGenerator astGenerator = new ASTGenerator(parseTree);
            ParseTree ast = astGenerator.generateAST();
            LLVMGenerator LLVMGenerator = new LLVMGenerator();
            LLVMGenerator.generate(ast);
            
            // Write to latex file

            try{
                File latexFile = new File(latexFilepath);
                FileWriter latexFileWriter = new FileWriter(latexFile);
                latexFileWriter.write(ast.toLaTeX());
                latexFileWriter.close();

                File llvmFile = new File(llvmFilepath);
                FileWriter llvmFileWriter = new FileWriter(llvmFile);
                llvmFileWriter.write(LLVMGenerator.getCode().toString());
                llvmFileWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred while writing the latex.");
                System.exit(1);
            }
        }
        else {
            System.out.println("Invalid command. See usage:");
            System.out.println("Usage: java -jar ./dist/part2.jar <inputFile>");
            System.out.println("   or: java -jar ./dist/part2.jar -wt <latexFile> <inputFile>");
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Parse the input file and return the parse tree.
     * @param filepath the path to the input file
     * @return the parse tree
     * @throws IOException Not able to find or open the file
     */
    private static ParseTree parse(String filepath) throws IOException {
        Parser parser = new Parser(filepath);
        parser.program();
        return parser.getParseTree();
    }
}
