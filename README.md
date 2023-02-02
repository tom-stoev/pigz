# pigzj

Pigzj introduces multithreading to the standard gzip compression algorithm. 

The main thread acts as a read thread and it continuously reads in new blocks as long as STDIN has more data to give. It submits a block that has been read to the compressor, which has a thread pool, and the write and checksum threads.

The write thread waits for blocks to arrive in its queue (which occurs after they are read). The write thread pops off the last block in its queue, if the queue has blocks, and waits for that block's corresponding countdown latch to decrement to zero. This occurs when the block has finished compressing. After which it writes the contents of the compressed block to the output stream. 

The checksum thread similarly has its own queue, but it does not need to wait for compression to finish. When a block is entered into its queue by the main read thread it is able to immediately begin updating the total checksum. 

Compile with 
```
javac *.java 
```

Compress with 
```
java Pigzj -p NUM_THREADS < input_file > output_file
```

Decompress with 
```
gzip -dv input_file > output_file
```

Sample usage
``` 
java Pigzj -p 3 < README.txt > readme.gz 
gzip -dv readme.gz > README2.txt
```
