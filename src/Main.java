import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Main.
 */
public class Main {
    /**
     * Main class that runs the LexicalAnalyzer.
     *
     * @param args the path to the input file
     * @throws IOException Not able to find or open the file
     */
    public static void main(String[] args) throws IOException {
        // Verify arguments entry
        if (args.length != 1) {
            System.out.println("Il faut passer un fichier en argument.");
            System.exit(1);
        }

        // Variable initialization
        String filepath = args[0];
        FileReader inputFile = new FileReader(filepath);
        LexicalAnalyzer lexer = new LexicalAnalyzer(inputFile);
        Map<Object, Integer> variableMap = new HashMap<>();

        // Create a FileOutputStream to write to the file
        FileOutputStream fileOutputStream = changeOutput(filepath.replace(".pmp", ".out"));

        // Scan the file token by token
        Symbol current_token = lexer.yylex();

        while (current_token.getType() != LexicalUnit.EOS){
            // Check if it is a variable
            if (current_token.getType() == LexicalUnit.VARNAME){
                addVariable(variableMap, current_token);
            }

            // Print the token
            System.out.println(current_token);

            // Get the next token
            current_token = lexer.yylex();
        }

        // Print variable table
        printVariableMap(variableMap);
        // Close the output file
        fileOutputStream.close();
    }

    /**
     * Add a variable and the line of its first occurrence to the variable map.
     * @param variableMap the map of variables and its line of first occurrence
     * @param current_token the current token send by the Lexer
     */
    private static void addVariable(Map<Object, Integer> variableMap, Symbol current_token) {
        // If it is a variable and it is not already in the map, add it with its line number.
        if (!variableMap.containsKey(current_token.getValue())){
            variableMap.put(current_token.getValue(), current_token.getLine());
        }
    }

    /**
     * Write the variable map to the output file.
     * @param variableMap the map of variables and its line of first occurrence
     */
    private static void printVariableMap(Map<Object, Integer> variableMap) {
        System.out.println("\nVariables");
        for (Map.Entry<Object, Integer> entry : variableMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    /**
     * Change the output stream to the output file.
     * @param outputFilePath the path to the output file
     * @return the FileOutputStream
     * @throws FileNotFoundException the file is not found
     */
    private static FileOutputStream changeOutput(String outputFilePath) throws FileNotFoundException {
        // Create a FileOutputStream to write to the file
        FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
        PrintStream printStream = new PrintStream(fileOutputStream);
        // Redirect the standard output stream to the PrintStream
        System.setOut(printStream);
        return fileOutputStream;
    }

}
