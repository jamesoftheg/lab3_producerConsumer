// package net.jcip.examples;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
public class ProducerConsumer2 {

    /**
     * The FileCrawler class is the producer, the thread starts at a given root in a directory and finds all files in the
     * hierarchy that will be processed by the indexer class.
     */
    static class FileCrawler implements Runnable {
        private final BlockingQueue<File> fileQueue;
        private final FileFilter fileFilter;
        private final File root;
        Set<File> visitedFiles = new HashSet<File>();

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
            // Skip the file, already checked, return true
            // Add it to the list and return false
            // string path - f.getPath();
            if (visitedFiles.contains(f) || visitedFiles.contains(f.getName())) {
                return true;
            } else {
                visitedFiles.add(f);
                return false;
            }
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
        private String filename;
        private static AtomicInteger count = new AtomicInteger(0);
        private final BlockingQueue<File> queue;

        /**
         * Indexer is the consumer, it runs forever at this point.
         * We need a poison object here.
         * @param queue
         */
        public Indexer(BlockingQueue<File> queue, String filename) {
            this.queue = queue;
            this.filename = filename;
        }

        public void run() {
            try {
                File poison = new File("poison.txt");
                // We check for the poison object
                while (true) {
                    // Next item being taken from the queue.
                    File next = queue.take();
                    if (poison.equals(next)) {
                        break;
                    }
                    if (next.getName().equals(filename) || next.getName().matches(filename) || next.getName().matches("(.*)"+ filename)) {
                        indexFile(next);
                    }
                }

                // Check for it here - don't process anymore, gracefully shut down threads.
                // When we find for this consumer, we return from the run method which will shut down the thread.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * We have to remember that if we have one counter and multiple threads, it's asynchronous.
         * Different threads could be updating it at the same time.
         * Protect it using mutual exclusion. Lock it, atomic variables, sync statements, etc.
         * @param file
         */
        public void indexFile(File file) {
            // Index the file...
            count.getAndIncrement();
            System.out.println(count);
            System.out.println(file);
            // Count should get to 101 in task 1.
            // ADD THE FILES HERE

        }

        ;
    }

    private static final int BOUND = 10;
    private static final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();

    /**
     * This method takes in an array of roots, which are of type File to know where to start the file crawler from.
     * Starts a producer thread for each root in the array and a consumer thread for each processor that is available.
     * @param roots
     */
    public static void startIndexing(File[] roots, String filename) {
        BlockingQueue<File> queue = new LinkedBlockingQueue<File>(BOUND);

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return true;
            }
        };
        for (File root : roots) new Thread(new FileCrawler(queue, filter, root)).start();

        // Create the ExecutorService threads
        ExecutorService executorService = Executors.newFixedThreadPool(N_CONSUMERS);
        executorService.execute(new Indexer(queue, filename));
        executorService.shutdown();
    }
}