import java.io.File;
import java.util.Scanner;

public class ProducerConsumerMain2 {

    public static void main(String[] args) {
        // Our root file is C:\test10183

        // Start the scanner
        Scanner keyboard = new Scanner(System.in);
        // User inputs the directories
        System.out.println("What is the directory?");
        String rootdir = keyboard.nextLine();
        File directory2 = new File(rootdir);
        File[] roots2 = new File[1];
        roots2[0] = directory2;
        // User inputs the name of the file being searched for
        System.out.println("What is the name of the file being looked for?");
        String filename = keyboard.nextLine();
        // boolean start = false;

        /**
         *
         This seemed like a good idea, but it wasn't
         while (!Objects.equals(filename, "exit")) {
         if (start) {
         System.out.println("What is the name of the file being looked for?");
         filename = keyboard.nextLine();
         ProducerConsumer2.startIndexing(roots2, filename);
         }
         if (!start) {
         ProducerConsumer2.startIndexing(roots2, filename);
         start = true;
         }
         }
         */

        // Task 2
        ProducerConsumer2.startIndexing(roots2, filename);

    }
}
