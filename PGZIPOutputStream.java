import java.util.zip.*;
import java.io.*;
import java.nio.file.*;


public class PGZIPOutputStream{
    private int num_threads = 0; 
    private ReadTask readTask; 
    private WriteTask writeTask; 
    private Thread writeThread; 
    private Compressor compressor; 
    private ChecksumTask checksumTask; 
    private Thread checksumThread; 
    private Block previousBlock = null;
	private Block currentBlock = null;
    
    // this class models the PGZIPOutputStream class in the MessAdmin implementation
    public PGZIPOutputStream(int threads) throws IOException{
        try {
        readTask = new ReadTask(); 
        compressor = new Compressor(threads);
        writeTask = new WriteTask();
        writeThread = new Thread(writeTask); 
        checksumTask = new ChecksumTask(); 
        checksumThread = new Thread(checksumTask); 
        checksumThread.start(); 
        writeThread.start();
        } catch (IOException e){
            System.err.print("IO Exception");
            System.exit(1);
        } catch (RuntimeException e){
            System.err.print("Runtime exception");
            System.exit(1);
        } catch (OutOfMemoryError e){
            System.err.print("no more memory");
            System.exit(1);
        }
    }

    public synchronized void write(InputStream in) throws IOException{
        readTask.setInput(in); 
        write(); 
    }

    private void write() throws IOException{
        Block currentBlock = readTask.getNextBlock(); 

        // similar structure to MESSADMIN implementation
        while(currentBlock != null){
            try {
            Block next = readTask.getNextBlock(); 
            if(next == null){
                currentBlock.setLastBlock();
                compressor.compressLastBlock(currentBlock, previousBlock); 
            } else {
                compressor.compress(currentBlock, previousBlock);
            }
            checksumTask.submit(currentBlock); 
            writeTask.submit(currentBlock); 
            previousBlock = currentBlock; 
            currentBlock = next;
            } catch (OutOfMemoryError e){
                System.err.print("no mem");
                System.exit(1);
            } catch (IOException e){
                System.err.print("io exception");
                System.exit(1);
            }
        } 
    }

    public void close(){
        compressor.shutdown(); 
        try {
        //checksumTask.finish(); 
        checksumThread.join(); 
        //writeTask.finish(); 
        writeThread.join();
        byte[] trailerBuf = new byte[8];
        writeTask.writeTrailer(trailerBuf, 0, (int)checksumTask.getCRC(), (int)readTask.getUncompressedSize());
        writeTask.done();
        } catch (InterruptedException e){

        } catch (IOException e){
            System.err.print("IOException");
            System.exit(1);
        }catch (OutOfMemoryError e){
            System.err.print("no more mem");
            System.exit(1);
        }
    }



}