import org.tartarus.snowball.*;

public class Main {
    /**
     * This is the main method. Database.run() is called to produce the inverted
     * index. Program.main(args) is call to initialize the user interface.
     * @param args this is not used for any purpose
     */
    public static void main(String[] args) {
        Database.run();
        Program.main(args);
    }
}
