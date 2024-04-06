#!/bin/bash
/usr/bin/time -v --output=casestudies/atm/exec_0.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_0.seq'  | tee casestudies/atm/exec_0.txt

/usr/bin/time -v --output=casestudies/atm/exec_1.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_1.seq'  | tee casestudies/atm/exec_1.txt

/usr/bin/time -v --output=casestudies/atm/exec_2.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_2.seq'  | tee casestudies/atm/exec_2.txt

/usr/bin/time -v --output=casestudies/atm/exec_3.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_3.seq'  | tee casestudies/atm/exec_3.txt

/usr/bin/time -v --output=casestudies/atm/exec_4.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_4.seq'  | tee casestudies/atm/exec_4.txt

/usr/bin/time -v --output=casestudies/atm/exec_5.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_5.seq'  | tee casestudies/atm/exec_5.txt

/usr/bin/time -v --output=casestudies/atm/exec_6.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_6.seq'  | tee casestudies/atm/exec_6.txt

/usr/bin/time -v --output=casestudies/atm/exec_7.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_7.seq'  | tee casestudies/atm/exec_7.txt

/usr/bin/time -v --output=casestudies/atm/exec_8.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_8.seq'  | tee casestudies/atm/exec_8.txt

/usr/bin/time -v --output=casestudies/atm/exec_9.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[](l -> <>(! l))' '-g=[]((p && ! l) -> m)' '-g=[]((! p) -> ((! m) && X(l)))' '-t=3600' '-s=aalta' '--seq=casestudies/atm/exec_9.seq'  | tee casestudies/atm/exec_9.txt

