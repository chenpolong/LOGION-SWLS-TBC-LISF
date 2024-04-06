#!/bin/bash
/usr/bin/time -v --output=casestudies/las/exec_0.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_0.seq'  | tee casestudies/las/exec_0.txt

/usr/bin/time -v --output=casestudies/las/exec_1.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_1.seq'  | tee casestudies/las/exec_1.txt

/usr/bin/time -v --output=casestudies/las/exec_2.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_2.seq'  | tee casestudies/las/exec_2.txt

/usr/bin/time -v --output=casestudies/las/exec_3.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_3.seq'  | tee casestudies/las/exec_3.txt

/usr/bin/time -v --output=casestudies/las/exec_4.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_4.seq'  | tee casestudies/las/exec_4.txt

/usr/bin/time -v --output=casestudies/las/exec_5.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_5.seq'  | tee casestudies/las/exec_5.txt

/usr/bin/time -v --output=casestudies/las/exec_6.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_6.seq'  | tee casestudies/las/exec_6.txt

/usr/bin/time -v --output=casestudies/las/exec_7.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_7.seq'  | tee casestudies/las/exec_7.txt

/usr/bin/time -v --output=casestudies/las/exec_8.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_8.seq'  | tee casestudies/las/exec_8.txt

/usr/bin/time -v --output=casestudies/las/exec_9.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-g=[](a -> m)' '-g=[]((c && (! n)) -> (! d))' '-g=[]((! n) -> (! i))' '-g=[]((c && m && n) -> d)' '-g=[]((! h) -> (! n))' '-t=3600' '-s=aalta' '--seq=casestudies/las/exec_9.seq'  | tee casestudies/las/exec_9.txt

