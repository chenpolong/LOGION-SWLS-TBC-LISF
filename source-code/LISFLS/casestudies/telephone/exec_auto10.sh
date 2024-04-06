#!/bin/bash
/usr/bin/time -v --output=casestudies/telephone/exec_0.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_0.seq'  | tee casestudies/telephone/exec_0.txt

/usr/bin/time -v --output=casestudies/telephone/exec_1.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_1.seq'  | tee casestudies/telephone/exec_1.txt

/usr/bin/time -v --output=casestudies/telephone/exec_2.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_2.seq'  | tee casestudies/telephone/exec_2.txt

/usr/bin/time -v --output=casestudies/telephone/exec_3.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_3.seq'  | tee casestudies/telephone/exec_3.txt

/usr/bin/time -v --output=casestudies/telephone/exec_4.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_4.seq'  | tee casestudies/telephone/exec_4.txt

/usr/bin/time -v --output=casestudies/telephone/exec_5.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_5.seq'  | tee casestudies/telephone/exec_5.txt

/usr/bin/time -v --output=casestudies/telephone/exec_6.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_6.seq'  | tee casestudies/telephone/exec_6.txt

/usr/bin/time -v --output=casestudies/telephone/exec_7.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_7.seq'  | tee casestudies/telephone/exec_7.txt

/usr/bin/time -v --output=casestudies/telephone/exec_8.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_8.seq'  | tee casestudies/telephone/exec_8.txt

/usr/bin/time -v --output=casestudies/telephone/exec_9.time timeout 3601 java -Xmx12g -ea -jar ./target/LOGION-run.jar '-d=[](o -> ! c)' '-d=[](c -> ! f)' '-d=[](o -> ! f)' '-g=[](c  -> (c  U (f || d)) )' '-g=[](c  -> (c  U (o || d)) )' '-t=3600' '-s=aalta' '--seq=casestudies/telephone/exec_9.seq'  | tee casestudies/telephone/exec_9.txt

