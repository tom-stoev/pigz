import java.util.zip.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WriteTask implements Runnable{
    private final CountDownLatch trailerSync = new CountDownLatch(2);//2: crc, uncompressedSize
    private volatile BlockingQueue<Block> queue;
    private final static int GZIP_MAGIC = 0x8b1f;
    public volatile boolean finished = false;
    public ByteArrayOutputStream outStream; 
    private volatile int checksum;
    private volatile long size;

    // credit to MessAdmin for ideas on structure of WriteTask class
    public WriteTask() throws IOException{
        this.outStream = new ByteArrayOutputStream(); 
        this.queue = new LinkedBlockingQueue<>();
        byte[] header = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
        header[0] = (byte) GZIP_MAGIC; 
        header[1] = (byte)(GZIP_MAGIC >> 8);
        header[2] = Deflater.DEFLATED;  
        //outStream.write(header, 0, 10); 
        System.out.write(header, 0, 10);
        if(System.out.checkError()){
            System.err.println("bad write");
            System.exit(1);
        }
    }



    public void run(){
        while(!finished){
                try {
                // Block p = tasks.remove();
                // if(p.getLastStatus()){
                //     finished=true;
                // }
                // process(p);
                Block p = queue.take();
                if(p.getLastStatus()){
                    finished=true;
                }
                process(p);
                
                } catch (InterruptedException e){
                } catch (IOException r){  
                    System.err.print("io error");
                    System.exit(1);
                }
            
        }
        
    }

    public void process(Block block) throws InterruptedException, IOException{
        block.waitUntilCanWrite(); 
        try{
        block.writeCompressedOut(); 
        } catch(IOException e){
            System.err.print("IO Exception");
            System.exit(1);
        } catch (OutOfMemoryError e){
            System.err.print("no more mem");
            System.exit(1);
        }
    }

    synchronized public void submit(Block b){
        try{
        queue.put(b);
        } catch(InterruptedException e){

        }
        //tasks.add(b); 
    }

    public void finish(){
        finished = true; 
    }

    public void setCRC(int crc){
        checksum = crc; 
    }

    public void setUncompressedSize(long size){
        this.size = size;
    }


    public void writeTrailer(byte[] buf, int offset, int crc32, int uncompressedBytes) throws IOException {
		writeInt(crc32, buf, offset); // CRC-32 of uncompr. data
		writeInt(uncompressedBytes, buf, offset + 4); // Number of uncompr. bytes
        try {
            System.out.write(buf);
            if(System.out.checkError()){
                System.err.println("bad write");
                System.exit(1);
            }
        } catch (IOException e){
            System.err.print("IO Exception"); 

        } catch (OutOfMemoryError e){
            System.err.print("out of mem");
        }
        //System.out.println("current size: " + outStream.size());
	}

	private static void writeInt(int i, byte[] buf, int offset) throws IOException {
		writeShort(i & 0xffff, buf, offset);
		writeShort((i >> 16) & 0xffff, buf, offset + 2);
	}

	private static void writeShort(int s, byte[] buf, int offset) throws IOException {
		buf[offset] = (byte)(s & 0xff);
		buf[offset + 1] = (byte)((s >> 8) & 0xff);
	}

    public void done(){
        //System.out.println("Size of this dude3: " + outStream.size());
        // try {
        //     outStream.writeTo(System.out);
        //     if(System.out.checkError()){
        //         System.err.print("write error");
        //         System.exit(1);
        //     }
        // }
        // catch (IOException e){
        //     System.err.print("write error");
        //     System.exit(1);
        // } catch (OutOfMemoryError e){
        //     System.err.print("no mem");
        //     System.exit(1);
        // }
    }

}