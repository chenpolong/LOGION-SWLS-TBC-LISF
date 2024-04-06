#!/bin/bash
java -Xmx12g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main '-d=[] (! hburst0 || ! hburst1)' '-d=[] (<> hready)' '-d=[] (hlock1 -> hbusreq1)' '-d=[] (hmastlock && (! hburst0 && ! hburst1) -> X (<> (! busreq)))' '-d=[] (hlock0 -> hbusreq0)' '-d=! hready && ! hbusreq0 && ! hlock0 && ! hbusreq1 && ! hlock1' '-g=[] (true && ! hmaster1 && ! hmaster0 -> (busreq <-> hbusreq0))' '-g=[](! hgrant1 -> (! hgrant1 U hbusreq1 || [] (! hgrant1)))' '-g=[](X (! start) -> (true && ! hmaster1 && ! hmaster0 <-> X (true && ! hmaster1 && ! hmaster0) && (hmastlock <-> X hmastlock)))' '-g=[](! decide -> (locked <-> X locked))' '-g=[](decide && X hgrant1 -> (hlock1 <-> X locked))' '-g=[](X (! start) -> (true && ! hmaster1 && hmaster0 <-> X (true && ! hmaster1 && hmaster0) && (hmastlock <-> X hmastlock)))' '-g=[](decide && X hgrant0 -> (hlock0 <-> X locked))' '-g=[](! decide -> (hgrant0 <-> X hgrant0 && (hgrant1 <-> X hgrant1)))' '-g=[](hready -> (hgrant0 <-> X (true && ! hmaster1 && ! hmaster0)))' '-g=[](! hbusreq0 && ! hbusreq1 && decide -> X hgrant0)' '-g=[] (! hready -> X (! start))' '-g=[](hbusreq0 -> <> (! hbusreq0 || (true && ! hmaster1 && ! hmaster0)))' '-g=[](hready -> (locked <-> X hmastlock))' '-g=[] (true && ! hmaster1 && hmaster0 -> (busreq <-> hbusreq1))' '-g=(decide && start && hgrant0 && true && ! hmaster1 && ! hmaster0 && ! hmastlock && ! hgrant1)' '-g=[](hbusreq1 -> <> (! hbusreq1 || (true && ! hmaster1 && hmaster0)))' '-g=[] (hmastlock && (! hburst0 && ! hburst1) && start -> X (! start U (! start && ! busreq) || [] (! start)))' '-g=[](hmastlock && (hburst0 && ! hburst1) && start && hready -> X (! start U (! start && hready && X (! start U (! start && hready && X (! start U (! start && hready) || [] (! start))) || [] (! start))) || [] (! start)))' '-g=[](hmastlock && (hburst0 && ! hburst1) && start && ! hready -> X (! start U (! start && hready && X (! start U (! start && hready && X (! start U (! start && hready && X (! start U (! start && hready) || [] (! start))) || [] (! start))) || [] (! start))) || [] (! start)))' '-g=[](hready -> (hgrant1 <-> X (true && ! hmaster1 && hmaster0)))' '-g=[](hbusreq0 && X hbusreq1 -> X (X (hgrant0 && hgrant1)))'
