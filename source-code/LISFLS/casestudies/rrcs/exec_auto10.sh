#!/bin/bash
/usr/bin/time -v --output=casestudies/rrcs/exec_0.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_0.seq'  | tee casestudies/rrcs/exec_0.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_1.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_1.seq'  | tee casestudies/rrcs/exec_1.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_2.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_2.seq'  | tee casestudies/rrcs/exec_2.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_3.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_3.seq'  | tee casestudies/rrcs/exec_3.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_4.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_4.seq'  | tee casestudies/rrcs/exec_4.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_5.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_5.seq'  | tee casestudies/rrcs/exec_5.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_6.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_6.seq'  | tee casestudies/rrcs/exec_6.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_7.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_7.seq'  | tee casestudies/rrcs/exec_7.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_8.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_8.seq'  | tee casestudies/rrcs/exec_8.txt

/usr/bin/time -v --output=casestudies/rrcs/exec_9.time timeout 3601 java -Xmx8g -ea -jar ./target/LOGION-run.jar '-d=[] (ta <-> X(tc))' '-d=[] (X(cc) -> ca && go)' '-g=[] (tc -> !cc)' '-g=[] (ta -> !go)' '-t=3600' '-s=aalta' '--seq=casestudies/rrcs/exec_9.seq'  | tee casestudies/rrcs/exec_9.txt

