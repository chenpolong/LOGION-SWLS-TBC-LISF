#!/bin/bash
java -Xmx12g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))'
