import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;


// credit to MessAdmin for ideas on structure of Block class 
public class Block{
    public volatile byte[] uncompressed;
	public volatile byte[] cmpBlockBuf; 
	private volatile int uncompressedSize;
	public volatile int compressedSize; 
	private volatile CountDownLatch writeSync;
	//private volatile CountDownLatch checksumSync;
    public int blockSize = 128*1024; 
	private volatile boolean is_last = false;
    protected volatile int blockNumber;
    private volatile ByteArrayOutputStream compressed;
	
    public Block(){
		compressed = new ByteArrayOutputStream();
        uncompressed  = new byte[blockSize];
		cmpBlockBuf = new byte[2*blockSize]; 
        initialize(); 
    }

    protected void initialize(){
        uncompressedSize = 0;
		compressedSize = 0;
		writeSync = new CountDownLatch(1);//1: compress
		//checksumSync = new CountDownLatch(1);
    }

    public int readFully(InputStream input) throws IOException{
		int totalRead = 0, lastRead = 0;
        if(input == null){
			System.exit(1);
        }
		while (lastRead != -1 && uncompressedSize < uncompressed.length) {
            try {
			lastRead = input.read(uncompressed, totalRead, uncompressed.length-totalRead);
            } catch (IOException e){
				System.err.print("IO Exception");
                System.exit(1); 
            } 
			if (lastRead > 0) {
				uncompressedSize += lastRead;
				totalRead += lastRead;
			}
		}
		return totalRead;
	}
    public boolean isComplete() {
		return uncompressedSize == uncompressed.length;
	}

	public byte[] getUncompressed(){
		return uncompressed; 
	}
	synchronized void setLastBlock(){
		is_last = true; 
	}

	synchronized boolean getLastStatus(){
		return is_last; 
	}

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	public void compressionDone(){
		writeSync.countDown(); 
	}

	public void waitUntilCanWrite() throws InterruptedException{
		writeSync.await();
	}


	public void writeCompressedOut() throws IOException {
		//System.out.println("size of compressed: " + compressed.size()); 
		try {
		compressed.writeTo(System.out);
		if(System.out.checkError()){
			System.err.print("write error");
			System.exit(1);
		}
		} catch (IOException e){
            System.err.print("io error");
            System.exit(1);
        }
	}

	public void writeCompressed(byte b[], int off, int len) {
		compressed.write(b, off, len);
	}






}