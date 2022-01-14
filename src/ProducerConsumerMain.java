import java.io.File;

public class ProducerConsumerMain {

    public static void main(String[] args) {
        File directory = new File("C:\\test10183");
        File[] roots = new File[1];
        roots[0] = directory;

        // Task 1
        ProducerConsumer.startIndexing(roots);
    }
}
