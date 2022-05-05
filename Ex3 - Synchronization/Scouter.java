// Dotan Beck - 313602641

import java.io.File;

public class Scouter extends Object implements Runnable {

    private SynchronizedQueue<File> dirQ;
    private File root;
    private int id; 
    private SynchronizedQueue<String> audQ; 
    private boolean isAudit; 

    public Scouter(SynchronizedQueue<File> dirQ, File root, int id, SynchronizedQueue<String> audQ, boolean isAudit)
    {
        this.dirQ = dirQ;
        this.root = root;
        this.id=id;
        this.audQ=audQ;
        this.isAudit=isAudit;

    }

    @Override
    public void run() {
        dirQ.registerProducer();
        

        SynchronizedQueue<File> direForSearch = new SynchronizedQueue<>(1000);
        direForSearch.enqueue(root);

        while(direForSearch.getSize()>0){
            File directory = direForSearch.dequeue();

            dirQ.enqueue(directory);

            if(isAudit){
                audQ.registerProducer();
                audQ.enqueue("Scouter on thread id "+this.id+": directory named "+directory.getName()+" was scouted");
                 audQ.unregisterProducer();
            } 


            File[] directoriesArray = directory.listFiles();

            
            if (directoriesArray != null) {
                for (File file : directoriesArray) {
                    if (file.isDirectory()) {
                        direForSearch.enqueue(file);
                    } // else dont do anything. 
                }
            }
        }
        dirQ.unregisterProducer();
       

    }
}
