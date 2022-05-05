// Dotan Beck - 313602641

import java.io.File;

public class DiskSearcher {
    public static void main(String[] args) throws Exception {
     
        boolean flag = Boolean.parseBoolean(args[0]);
        String prefix = args[1];
        String rootPath = args[2];
        String destPath = args[3];
        int searchersCount = Integer.parseInt(args[4]);
        int copiersCount= Integer.parseInt(args[5]);
        File root = new File(rootPath);
        File destFile = new File(destPath);

        //milstonesQ
        SynchronizedQueue<String> milestonesQueue = new SynchronizedQueue<>(1000);
        milestonesQueue.enqueue("General, program has started the search");

        //init dirQ
        SynchronizedQueue<File> dirQ = new SynchronizedQueue<>(1000);

        //start Scounter
        Scouter scouter = new Scouter(dirQ, root, 1, milestonesQueue, flag);
        Thread scouterThread = new Thread(scouter);
        scouterThread.start();
        scouterThread.join();

        //init resQ
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(1000);

        //start mulipul searchers
        Thread[] searchers = new Thread[searchersCount];
        for (int i = 0 ; i < searchersCount ; i++) {
            searchers[i] = new Thread(new Searcher(prefix,dirQ, resultsQueue, i+2, milestonesQueue, flag));
            searchers[i].start();
        }

        //start mulipul copiers
        Thread[] copiers = new Thread[copiersCount];
        for (int i = 0 ; i < copiersCount ; i++) {
           copiers[i] = new Thread(new Copier(i+searchersCount+2,destFile, resultsQueue, milestonesQueue, flag));
           copiers[i].start();
        }
      

        String finalStr = milestonesQueue.dequeue();
        int counter = 1;
        while(finalStr!= null){
            System.out.println(counter + ": "+ finalStr);
            finalStr = milestonesQueue.dequeue();
            counter++;
        }

    
    }
}
