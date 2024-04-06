#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-g=[] (p -> <>(q))' '-g=[] (q -> p)'