#!/bin/bash
java -Xmx12g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )'
