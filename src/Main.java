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
        String filepath = "" ;
        Parser parser ;

        // Verify arguments entry
        if (args.length < 1) {
            System.out.println("Il faut passer un fichier en argument.");
            System.exit(1);
        }

        // In case multiple input are passed in parameters
        int i = 0;
        while (i < args.length) {
            filepath = args[i];
            i++;
        }
        
        parser = new Parser(filepath);
        for (int j = 0; j < 10; j++){
        parser.printToken();
        parser.nextToken();}

    }
}
