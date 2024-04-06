#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))'