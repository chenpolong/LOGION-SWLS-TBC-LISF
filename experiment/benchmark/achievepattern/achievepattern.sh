#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-d=[] (q -> s)' '-g=[] (p -> <>(q))' '-g=[] (r -> [](!s))'