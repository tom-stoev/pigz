import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.io.*;

// credit to MessAdmin for ideas on structure of Compressor class
public class Compressor{
    protected ThreadPoolExecutor compressExecutor; 
    public Compressor(int size_pool){
        compressExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(size_pool);
    }

    public void compress(Block curr, Block prev){
        try{
        Runnable compressTask = new CompressTask(curr, prev, true); 
        compressExecutor.execute(compressTask);
        }catch (OutOfMemoryError e){
            System.err.print("no more memory");
            System.exit(1);
        } 
        //System.out.println("pool size:" + compressExecutor.getPoolSize());
    }

    public void compressLastBlock(Block curr, Block prev){
        try{
        Runnable compressTask = new CompressTask(curr, prev, false); 
        compressExecutor.execute(compressTask);
        } catch(OutOfMemoryError e){
            System.err.print("no more memory");
            System.exit(1);
        }
    }

    public void shutdown() {
		compressExecutor.shutdown();
	}    


}