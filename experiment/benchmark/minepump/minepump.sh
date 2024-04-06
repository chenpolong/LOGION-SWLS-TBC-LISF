#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-d=[]((p && X(p)) -> X(X(! h)))' '-g=[](h -> X(p))' '-g=[](m -> X(! p))'
