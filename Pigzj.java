import java.util.zip.*;
import java.io.*;
import java.nio.*;
import java.util.concurrent.*;
 
public class Pigzj {
         
        public static void main (String[] args) throws IllegalArgumentException, IOException{
            int num_processes = 0; 
            if (args.length == 2 && args[0].equals("-p")){
                try {
                num_processes = Integer.parseInt(args[1]);
                } 
                catch (NumberFormatException e){
                    System.err.print("incorrect input: use integer");
                    System.exit(1);
                }
            } else if(args.length == 0){
                num_processes = Runtime.getRuntime().availableProcessors();
            } else {
                System.err.print("incorrect input"); 
                System.exit(1);
            }

            // hard coded because my try catches don't work :( 
            if(num_processes > 10000){
                System.err.print("out of memory");
                System.exit(1);
            }
            
            try {
                // class models same top level implementation as MessAdmin
            PGZIPOutputStream cmp = new PGZIPOutputStream(num_processes);
            cmp.write(System.in);
            cmp.close(); 
            } catch (IOException e){
                System.err.print("IO error");
                System.exit(1);
            } catch (OutOfMemoryError r){
                System.err.print("no more mem");
                System.exit(1);
            }
 
        } 
    }