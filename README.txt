                    HW 3 REPORT     

Implementation
I got inspiration from the MessAdmin implementation and thus 
used a same top level approach for my Pigzj implementation. 

Block Class: 
The block class contains compressed and uncompressed buffers and 
corresponding counters. Furthermore, it contains a countdown latch 
which serves the purpose of letting the writeTask thread know when 
it can safely write the contents of the compressed buffer out. 
The block class is also in charge of reading from STDIN and its 
readFully function is invoked by the readTask (run by main thread)
which fills the contents of the uncompressed buffer accordingly. 


PGZIPOutputStream class: 
This class spawns two threads, a write thread
and a checksum thread. Each of these threads gets passed in a  
Task which implements runnable (WriteTask and ChecksumTask) respectively.

The main thread acts as a read thread and it constantly reads 
in new blocks as long as STDIN has more data to give. It 
will call the compressor class "compress" method, or the 
"compressLastBlock" method depending on if the current block is the 
last one in the input stream. 
Furthermore, the main thread will submit a block to the queues maintained
inside of WriteTask and ChecksumTask. 

Compressor class:
The compressor class has a threadpool of a fixed size which is either 
the maximum number of available processors on the machine or the number of 
processes passed in as an argument. When the compressor is passed in a 
current block and a previous block it will create a CompressTask that takes 
in as arguments these blocks (along with a boolean indicating it is the last block).

CompressTask: 
If the previous block is not null then we can prime the dictionary of the deflator 
for the current block. If the current block is not the last one we will use 
SYNC_FLUSH with the deflator. The compress task just compresses the block and writes it 
to the compressedBuffer after which it decrements a countdown latch for the current block 
object passed in. When this countdown latch is decremented the write thread will stop waiting 
on the latch and will then write the compressed bytes to its ByteArrayOutputStream maintained 
internally. 

WriteTask: 
The writetask implements runnable. In its run function it essentially loops forever until 
the main thread in PGZIPOutputStream calls its finish() function. After which if the queue is 
empty it will stop looping and become joined in the PGZIPOutputStream class. 
During the loop in its RUN() method the writetask constantly checks if its internal queue of 
blocks is empty. If the queue is not empty it will pop off the last block and wait for its 
corresponding countdown latch to be decremented to zero. After which it will know that the 
compression is done and can write the contents of the blocks compressed buffer to its output 
stream. The writetask does not need to worry about the ordering in the queue because the main 
thread in the PGZIPOutputStream submits all the tasks in the order by which they are read in 
from STDIN. By using these countdown latches we are able to ensure that writing to the stream 
always occurs after compression. 

ChecksumTask: 
The checksumtask also implements runnable. Its run method works in a nearly identical way 
to WriteTask. However, checksum can happen as soon as the block is read in and thus the 
ChecksumTask does not need to wait for compression. The elements in the internal queue for 
checksum are also inserted in order by the main thread. Thus, as long as the finish() 
method is not called by the main thread or there are remaining elements in the internal 
queue the ChecksumTask will continue to pop elements off and update the total checksum. 


Performance:
RELATIVE TO GZIP 

input=/usr/local/cs/jdk-17.0.1/lib/modules
GZIP
time gzip <$input >gzip.gz
real    0m7.509s
user    0m7.381s
sys     0m0.076s

PIGZJ 
with 1 Thread (3 TRIALS)
time java Pigzj -p 1 <$input >Pigzj.gz
    real    0m7.990s
    user    0m7.690s
    sys     0m0.588s

    real    0m7.922s
    user    0m7.644s
    sys     0m0.595s

    real    0m7.914s
    user    0m7.631s
    sys     0m0.582s

with 2 Threads (3 TRIALS)
time java Pigzj -p 2 <$input >Pigzj.gz
    real    0m4.311s
    user    0m7.640s
    sys     0m0.578s

    real    0m4.294s
    user    0m7.633s
    sys     0m0.599s

    real    0m4.281s
    user    0m7.605s
    sys     0m0.633s

with 3 Threads (3 TRIALS)
time java Pigzj -p 3 <$input >Pigzj.gz
    real    0m3.116s
    user    0m7.638s
    sys     0m0.605s

    real    0m3.146s
    user    0m7.637s
    sys     0m0.558s

    real    0m3.096s
    user    0m7.589s
    sys     0m0.639s

with 4 Threads (3 TRIALS)
time java Pigzj -p 4 <$input >Pigzj.gz
    real    0m2.667s
    user    0m7.652s
    sys     0m0.547s

    real    0m2.769s
    user    0m7.655s
    sys     0m0.528s

    real    0m2.742s
    user    0m7.662s
    sys     0m0.541s

with 6 Threads (3 TRIALS)
time java Pigzj -p 6 <$input >Pigzj.gz
    real    0m2.968s
    user    0m7.680s
    sys     0m0.525s

    real    0m2.857s
    user    0m7.652s
    sys     0m0.559s

    real    0m2.867s
    user    0m7.696s
    sys     0m0.442s

From the benchmarks, PIGZJ outperformed GZIP in every instance 
where number of threads > 1. When the threads were equal to 1, 
they performed similarly.

Relative to pigz:

Format of following tests: 
input=/usr/local/cs/jdk-17.0.1/lib/modules
time java Pigzj -p NUMTHREADS <$input >Pigzj.gz
time pigz -p NUMTHREADS <$input >Pigzj.gz

COMPRESSION RATIOS
FILE SIZE: 126421170
                          COMPRESSED SIZE     RATIO
Pigzj.gz                   43262591           2.92218212266
gzip.gz                    43381443           2.91417622968
pigz.gz                    43261121           2.92228141753

Overall they have similar compression ratios. GZIP performs 
slightly better in regards to compression whereas Pigzj and pigz 
have nearly the same compression ratios. 

There are some discrepancies between pigz and Pigzj, this is likey due 
to pigz storing the file name,and likely some other configuration data. 


For 1 Thread:
    Pigzj vvv

    real    0m7.919s
    user    0m7.628s
    sys     0m0.584s

    pigz vvv

    real    0m7.432s
    user    0m7.274s
    sys     0m0.091s

    Pigzj vvv

    real    0m7.916s
    user    0m7.651s
    sys     0m0.569s

    pigz vvv

    real    0m7.393s
    user    0m7.272s
    sys     0m0.076s

    Pigzj vvv

    real    0m7.934s
    user    0m7.649s
    sys     0m0.590s

    pigz vvv

    real    0m7.524s
    user    0m7.278s
    sys     0m0.076s

For 2 Threads:
    Pigzj vvv

    real    0m4.298s
    user    0m7.656s
    sys     0m0.582s

    pigz vvv

    real    0m4.409s
    user    0m7.356s
    sys     0m0.231s

    Pigzj vvv

    real    0m4.363s
    user    0m7.637s
    sys     0m0.659s

    pigz vvv

    real    0m3.817s
    user    0m7.287s
    sys     0m0.208s

    Pigzj vvv

    real    0m4.549s
    user    0m7.648s
    sys     0m0.553s

    pigz vvv

    real    0m3.742s
    user    0m7.330s
    sys     0m0.160s

For 3 Threads:
    Pigzj vvv

    real    0m3.133s
    user    0m7.682s
    sys     0m0.537s

    pigz vvv

    real    0m2.621s
    user    0m7.280s
    sys     0m0.220s

    Pigzj vvv

    real    0m3.119s
    user    0m7.610s
    sys     0m0.598s

    pigz vvv

    real    0m2.638s
    user    0m7.302s
    sys     0m0.189s

    Pigzj vvv

    real    0m3.186s
    user    0m7.635s
    sys     0m0.568s

    pigz vvv

    real    0m2.630s
    user    0m7.346s
    sys     0m0.167s


For 4 Threads:
    Pigzj vvv

    real    0m2.824s
    user    0m7.664s
    sys     0m0.579s

    pigz vvv

    real    0m2.359s
    user    0m7.385s
    sys     0m0.136s

    Pigzj vvv

    real    0m2.688s
    user    0m7.683s
    sys     0m0.534s

    pigz vvv

    real    0m2.134s
    user    0m7.330s
    sys     0m0.145s

    Pigzj vvv

    real    0m2.787s
    user    0m7.640s
    sys     0m0.570s

    pigz vvv

    real    0m2.120s
    user    0m7.365s
    sys     0m0.110s

For 6 Threads:
    Pigzj vvv

    real    0m3.111s
    user    0m7.708s
    sys     0m0.536s

    pigz vvv

    real    0m2.433s
    user    0m7.391s
    sys     0m0.169s

    Pigzj vvv

    real    0m3.130s
    user    0m7.734s
    sys     0m0.429s

    pigz vvv

    real    0m2.357s
    user    0m7.395s
    sys     0m0.140s

    Pigzj vvv

    real    0m3.067s
    user    0m7.733s
    sys     0m0.489s

    pigz vvv

    real    0m2.335s
    user    0m7.384s
    sys     0m0.163s

Pigzj is competitive in speed relative to pigz. However, 
pigz does tend to have a slightly faster computational time. 
This is likey due to a little more lock contention in the Pigzj 
implementation. 


STRACE OUTPUT: 
gzip
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 65.99    0.003376          20       166           write
 20.35    0.001041           0      3861           read
  9.15    0.000468         468         1           execve
  1.11    0.000057           4        12           rt_sigaction
  0.84    0.000043           7         6           mmap
  0.82    0.000042          10         4           mprotect
  0.37    0.000019           6         3           fstat
  0.31    0.000016          16         1           munmap
  0.29    0.000015           7         2           openat
  0.18    0.000009           2         4           close
  0.18    0.000009           4         2         1 arch_prctl
  0.16    0.000008           8         1         1 access
  0.10    0.000005           5         1         1 ioctl
  0.08    0.000004           4         1           lseek
  0.08    0.000004           4         1           brk
------ ----------- ----------- --------- --------- ----------------
100.00    0.005116           1      4066         3 total

pigz
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 74.75    0.071965         114       627         3 futex
 23.43    0.022553          23       975           read
  0.64    0.000616         123         5           clone
  0.49    0.000469         469         1           execve
  0.21    0.000200           9        22           munmap
  0.14    0.000136           4        28           mmap
  0.12    0.000112           7        15           mprotect
  0.06    0.000059           9         6           openat
  0.03    0.000032           5         6           fstat
  0.03    0.000030           5         6           close
  0.02    0.000022           2         8           brk
  0.02    0.000016           5         3           rt_sigaction
  0.01    0.000014           4         3           lseek
  0.01    0.000010           5         2         2 ioctl
  0.01    0.000009           4         2         1 arch_prctl
  0.01    0.000008           8         1         1 access
  0.01    0.000005           5         1           rt_sigprocmask
  0.00    0.000004           4         1           set_tid_address
  0.00    0.000004           4         1           set_robust_list
  0.00    0.000004           4         1           prlimit64
------ ----------- ----------- --------- --------- ----------------
100.00    0.096268          56      1714         7 total

Pigzj
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 99.61    0.516248       86041         6         4 futex
  0.10    0.000528           9        56        45 openat
  0.09    0.000471         471         1           execve
  0.05    0.000247           6        39        36 stat
  0.04    0.000220           8        25           mmap
  0.03    0.000170          10        16           mprotect
  0.02    0.000081           6        13           read
  0.01    0.000058           5        11           fstat
  0.01    0.000055           5        11           close
  0.01    0.000034          17         2           munmap
  0.01    0.000033          16         2           readlink
  0.01    0.000026          26         1           clone
  0.00    0.000021           5         4           brk
  0.00    0.000019           9         2         1 access
  0.00    0.000014           4         3           lseek
  0.00    0.000009           4         2           rt_sigaction
  0.00    0.000009           9         1           set_tid_address
  0.00    0.000005           5         1           set_robust_list
  0.00    0.000004           4         1           rt_sigprocmask
  0.00    0.000004           2         2         1 arch_prctl
  0.00    0.000004           4         1           prlimit64
  0.00    0.000000           0         2           getpid
------ ----------- ----------- --------- --------- ----------------
100.00    0.518260        2565       202        87 total

The strace for the compression programs explains why Pigzj spends more
time in kernel mode because the usecs/call for the futex calls is large (suggesting that
there is a lot of time spent waiting for a thread to join, or to grab a lock)

In general the GZIP has less overall sys time. This explains the benchmarks and 
how GZIP consistently scores the lowest in that category. 
Furthermore the strace explains why pigz and Pigzj are faster, because they make 
futex calls which are necessary for locks which is part of parallelism and 
maintaining concurrency. The absence of such system calls reflects that gzip is 
in fact single threaded. The use of a single thread is a lot slower (and will scale
as the file size increases) because a lot of time is wasted waiting on 
compression.


After-Action Report
Pigzj does a successful job of implementing multithreaded compression. It performs 
significantly better than gzip when using a number of threads > 1. When it only uses 
a single thread it works at relatively the same speed as gzip. 

In general I expect that as the size of the file scales that gzip would perform 
worse and worse relative to pigz and Pigzj because more and more time will be spent 
waiting on compression to finish. Thus Pigzj and pigz will be able to keep increasing
the gap by having other processes running tasks in parallel such as writing the compressed 
contents to an outputstream or updating the checksum or simultaneously reading in new 
blocks from STDIN.

As the size of the threads scales, I expect up to a certain threshold for the pigz and 
Pigzj to perform incredibly well. Once there are too many threads, where threads >> 
number of processors, we can expect to see a decline in performance as threads fight 
for CPU time. However, by default both implementations will use the maximum number of 
available processors on the machine (assuming no parameters passed in) and so 
in the general use case it would be optimal to use Pigzj or pigz. 

Difference from pigz: 
I use the same approach to the multithreaded compression by having a writeTask thread, 
a checkSumThread, a (main) read thread, as well as a compressor that is an executor pool. 
However, I do not extend support for older versions of java, and my program is much 
shorter and eaiser to read. Furthermore, Pigzj is set up so that it reads from 
STDIN to STDOUT while pigz traditionally reads from a file. Additionally, my program
does not use a BlockManager, any configurations, etc. It includes merely a 
multithreaded approach with some inspirations from both the Piazza hint code 
(SingleThreadedGZIPCompressor) and messAdmin. 

Credits: 
I read through the MessAdmin code which gave me several ideas on how I should 
structure the classes and the algorithm for the multithreaded compression in 
general. For instance, MessAdmin has a main thread which reads through a file 
and passes the blocks to a Compressor, a WriteTask thread, and a ChecksumTask 
thread. My implementation has the same structure. Furthermore when I was implementing 
the program I had to store a lot of data about the read in chunks of data so I adopted 
a Block class the held together a subset of the bundle of data represented in 
MessAdmin's block class. I also got inspiration from the class file names. However, 
I did not use a BlockManager and in general my code is highly simplified and easy to 
follow. Pigzj does essentially just what it needs to, without any flashy and 
useless excess functions.


