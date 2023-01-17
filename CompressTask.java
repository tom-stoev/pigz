import java.util.zip.Deflater;
import java.io.*;
// credit to MessAdmin for ideas on structure of CompressTask class
public class CompressTask implements Runnable{
    private Block b; 
    private static final int DICTIONARY_SIZE = 32*1024;
    private byte[] dictBuf; 
    private Deflater def; 
    private volatile int setting; 

    public CompressTask(Block curr, Block prev, boolean use_synch){
        if(use_synch){
            setting = Deflater.SYNC_FLUSH; 
        } else {
            setting = Deflater.NO_FLUSH; 
        }
        b = curr; 
        dictBuf = new byte[DICTIONARY_SIZE];
        def = new Deflater(Deflater.DEFAULT_COMPRESSION, true); 
        if (prev != null){
            System.arraycopy(prev.getUncompressed(), prev.blockSize - DICTIONARY_SIZE, dictBuf, 0, DICTIONARY_SIZE);
            def.setDictionary(dictBuf);
        }
    }

    public void run(){
        def.setInput(b.getUncompressed(), 0, b.getUncompressedSize()); 
        // if(setting == Deflater.SYNC_FLUSH){
        //     System.out.println("1");
        // } else {
        //     System.out.println("2");
        // }
        if(setting == Deflater.NO_FLUSH){
            if(!def.finished()){
                def.finish();
            }
        }
        int sum = 0; 
        int deflatedBytes = def.deflate(b.cmpBlockBuf, 0, b.cmpBlockBuf.length, setting);

        //System.out.print("def bytes: " + deflatedBytes);
        b.compressedSize = deflatedBytes; 
        if(deflatedBytes > 0){
            b.writeCompressed(b.cmpBlockBuf, 0, deflatedBytes); 
        }
        b.compressionDone(); 
    }
}