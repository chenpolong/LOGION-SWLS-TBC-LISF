#!/bin/bash
java -Xmx8g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-g=[](send -> (!ack U delivered))' '-g=[] (delivered -> (!send U ack))'