// Dotan Beck 313602641

import java.io.File;
import java.util.Objects;


public class Searcher extends Object implements Runnable {

    private String prefix;
    private SynchronizedQueue<File> dirQ;
    private SynchronizedQueue<File> resQ;
    private int id;
    private SynchronizedQueue<String> audQ;
    private boolean isAudit;

    public Searcher (String prefix, SynchronizedQueue<File> dirQ,SynchronizedQueue<File> resQ,int id,SynchronizedQueue<String> audQ,boolean isAudit)
    {
        this.prefix=prefix;
        this.dirQ=dirQ;
        this.resQ=resQ;
        this.id =id;
        this.audQ=audQ;
        this.isAudit=isAudit;
    }

    @Override
    public void run() {
        resQ.registerProducer();
        File f = dirQ.dequeue();

        synchronized (this) {

            while (f != null) {
                File[] files = f.listFiles();
                for (int i = 0; i < Objects.requireNonNull(files).length; i++) {

                    if (files[i].getName().startsWith(this.prefix)) {
                        resQ.enqueue(files[i]);

                        if (isAudit) {
                            audQ.registerProducer();
                            audQ.enqueue("Searcher on thread id " + this.id + ": file named " + files[i].getName() + " was found");
                            audQ.unregisterProducer();
                        } //else dont do anything
                    }
                }

                f = dirQ.dequeue();
            }

        }

        resQ.unregisterProducer();
        
    }
}
