import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {

    public void main(String[] args) throws FileNotFoundException {
        if (args.length != 1){
            System.out.println("Il faut passer un fichier en argument.");
            System.exit(0);
        }

        FileReader inputFile = new FileReader(args[0]);
    }
}
