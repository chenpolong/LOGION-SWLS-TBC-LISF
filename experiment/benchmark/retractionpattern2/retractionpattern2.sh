#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-g=[] (p -> (q W s))' '-g=[] (q -> r)'