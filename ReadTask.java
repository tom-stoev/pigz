import java.io.IOException;
import java.io.InputStream;

// credit to MessAdmin for ideas on structure of ReadTask class
public class ReadTask{
    private InputStream inputStream = null;
    private long uncompressedSize = 0; 
    private boolean finish = false; 
    public int BLOCK_SIZE = 128 * 1024; 
    public int DICT_SIZE = 32 * 1024; 

    public ReadTask(){
    }

    public void setInput(InputStream in){
        inputStream = in; 
    }

    public Block getNextBlock() throws IOException{ 
        try{
        Block block = new Block();
        int nRead = block.readFully(inputStream);
        if (nRead > 0) {
            uncompressedSize += nRead;
        }
        if(nRead == 0){
            return null;
        }
        return block;  
        } catch (OutOfMemoryError e){
            System.err.print("oops, no memory");
            System.exit(1);
            return null;
        } catch (IOException e){
            System.err.print("write error");
            System.exit(1);
            return null;
        }
        
        
    }

    public long getUncompressedSize(){
        return uncompressedSize; 
    }

    

}