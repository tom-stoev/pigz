import java.util.zip.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
// credit to MessAdmin for ideas on structure of ChecksumTask class
public class ChecksumTask implements Runnable{
    protected volatile CRC32 checksum; 
    private volatile boolean finished = false; 
    private volatile int num_tasks; 
    private volatile BlockingQueue<Block> queue;
    public ChecksumTask(){
        this.num_tasks = 0;
        this.checksum = new CRC32(); 
        this.queue = new LinkedBlockingQueue<>();
        checksum.reset(); 
    }

    public void run(){
        while(!finished){ 
            try{
                Block p = queue.take();
                if(p.getLastStatus()){
                    finished = true;
                }
                process(p);
            }catch (InterruptedException e){

            }
        }
    }
    protected void process(Block block){
        checksum.update(block.getUncompressed(), 0, block.getUncompressedSize());
    }

    synchronized public void submit(Block b){
        try {
        queue.put(b);
        } catch (InterruptedException e){

        }
        //tasks.add(b);
        //this.num_tasks+=1;
        //System.out.println("total checksum tasks = " + task_num);
    }

    public long getCRC(){
        //System.out.print("two");
        return checksum.getValue();
    }

    public void finish(){
        finished = true; 
    }


}