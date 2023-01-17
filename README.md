# pigzj

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
