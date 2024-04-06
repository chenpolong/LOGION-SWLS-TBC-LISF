#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)'
