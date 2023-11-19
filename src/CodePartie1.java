import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

public class CodePartie1 {
    
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
