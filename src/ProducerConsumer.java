// package net.jcip.examples;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ProducerConsumer * <p/> * Producer and consumer tasks in a desktop search application * * @author Brian Goetz and Tim Peierls
 */

/**
 * How many consumers we started, a poison object for every thread we create for consumer
 * We need to keep track and know how many poison objects to put on.
 * Task 2 is the first time we worry about overlap
 */
public class ProducerConsumer {

    /**
     * The FileCrawler class is the producer, the thread starts at a given root in a directory and finds all files in the
     * hierarchy that will be processed by the indexer class.
     */
    static class FileCrawler implements Runnable {
        private final BlockingQueue<File> fileQueue;
        private final FileFilter fileFilter;
        private final File root;


        // FileCrawler is the producer
        public FileCrawler(BlockingQueue<File> fileQueue, final FileFilter fileFilter, File root) {
            this.fileQueue = fileQueue;
            this.root = root;
            this.fileFilter = new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || fileFilter.accept(f);
                }
            };
        }

        /**
         * Keep a data structure of all the files you have visited already.
         * Does it exist already? If it's in the list, we skip it.
         * Skip the file, already checked, return true, else add it to the list and return false.
         * string path - f.getPath();
         * Protect the data structure, use mutual exclusion.
         * @param f
         * @return
         */
        private boolean alreadyIndexed(File f) {
            return false;
            // Skip the file, already checked, return true
            // Add it to the list and return false
            // string path - f.getPath();
        }

        // The run method crawls and goes through the loop, then exits
        // Before the run exits, we submit our poison object
        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Submit poison objects use File (String)
            // Submit poison objects use File (String)
            File poison = new File("poison.txt");
            int threads = java.lang.Thread.activeCount();
            for (int i = 0; i < threads; i++) {
                fileQueue.add(poison);
            }
        }

        private void crawl(File root) throws InterruptedException {
            File[] entries = root.listFiles(fileFilter);
            if (entries != null) {
                // This loop is set, it goes for a finite number of runs
                // We can create a file object with our poison string
                // It needs to be embedded in ou
                for (File entry : entries)
                    if (entry.isDirectory()) crawl(entry);
                    else if (!alreadyIndexed(entry)) fileQueue.put(entry);
            }
        }
    }

    /**
     * The Indexed is our consumer class. It needs to check for the poisoned object.
     * Code is processed in the indexFile method.
     */
    static class Indexer implements Runnable {
        private static AtomicInteger count = new AtomicInteger(0);
        private final BlockingQueue<File> queue;
        private boolean printed = false;

        /**
         * Indexer is the consumer, it runs forever at this point.
         * We need a poison object here.
         * @param queue
         */
        public Indexer(BlockingQueue<File> queue) {
            this.queue = queue;
        }

        public void run() {
            try {
                File poison = new File("poison.txt");
                // System.out.println(java.lang.Thread.activeCount());
                // We check for the poison object
                while (true) {
                    // Next item being taken from the queue.
                    File next = queue.take();

                    if (poison.equals(next)) {
                        break;
                    }
                    else {
                        indexFile(next);
                    }
                }

                // Check for it here - don't process anymore, gracefully shut down threads.
                // When we find for this consumer, we return from the run method which will shut down the thread.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Count: " + count);
        }

        /**
         * I really liked my dumb solution though. Behold.
         *
        public void run() {
            try {
                File match = new File("C:\\test10183\\USA\\Washington\\aplace.txt");
                File poison = new File("poison.txt");
                // We check for the poison object
                while (true) {
                    File next = queue.take();
                    if (match.equals(next)) {
                        indexFile(next);
                        int threads = java.lang.Thread.activeCount();
                        for (int i = 0; i < threads; i++) {
                            queue.add(poison);
                        }
                        break;
                    }
                    if (poison.equals(next)) {
                        break;
                    }
                    else {
                        indexFile(next);
                    }
                }
                // Check for it here - don't process anymore, gracefully shut down threads.
                // When we find for this consumer, we return from the run method which will shut down the thread.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        */

        /**
         * We have to remember that if we have one counter and multiple threads, it's asynchronous.
         * Different threads could be updating it at the same time.
         * Protect it using mutual exclusion. Lock it, atomic variables, sync statements, etc.
         * @param file
         */
        public void indexFile(File file) {
            // Index the file...
            count.getAndIncrement();
            // System.out.println(count);
            // System.out.println(file);
            // Count should get to 101 in task 1.
        }

    }

    private static final int BOUND = 10;
    private static final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();

    /**
     * This method takes in an array of roots, which are of type File to know where to start the file crawler from.
     * Starts a producer thread for each root in the array and a consumer thread for each processor that is available.
     * @param roots
     */
    public static void startIndexing(File[] roots) {
        BlockingQueue<File> queue = new LinkedBlockingQueue<File>(BOUND);

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return true;
            }
        };
        for (File root : roots) new Thread(new FileCrawler(queue, filter, root)).start();
        for (int i = 0; i < N_CONSUMERS; i++)
            new Thread(new Indexer(queue)).start();
    }
}