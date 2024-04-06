#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-d=[] ( X(open) -> atfloor)' '-g=[](call -> <>(open))'