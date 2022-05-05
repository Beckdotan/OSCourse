// Dotan Beck - 313602641   

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;



public class Copier extends Object implements Runnable {

    private static final int COPY_BUFFER_SIZE=0;
    private int id;
    private File dest;
    private SynchronizedQueue<File> resQ;
    private SynchronizedQueue<String> audQ;
    private boolean isAudit;

    public Copier(int id, File dest, SynchronizedQueue<File> resQ, SynchronizedQueue<String> audQ, boolean isAudit)
    {
         this.id=id;
         this.dest=dest;
         this.resQ=resQ;
         this.audQ=audQ;
         this.isAudit=isAudit;
    }

    @Override
    public void run() {

        
        File file = resQ.dequeue();

        synchronized(this){

            while ( file != null) {

                try {
                    Files.copy(file.toPath(), Paths.get(this.dest.toString(), file.getName()),StandardCopyOption.REPLACE_EXISTING /* to make sure its replacing the exsisting file */);
                    
                    if (isAudit) {
                        audQ.registerProducer();
                        audQ.enqueue("Copier from thread id " + this.id + ": file named " + file.getName() + " was copied");
                        audQ.unregisterProducer();
                    } // else do nothing

                } catch (IOException e) {
                    e.printStackTrace();
                }

                file = resQ.dequeue();

            }
        }
        
    }
}
