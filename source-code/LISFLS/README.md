# LISFLS
The code for `Lasso-based Incremental Satisfiability Filter for Local Searching More General Boundary Conditions in Requirements Specification`

## Build
```
chmod +x aalta_linux
chmod +x pltl
chmod +x nuXmv
chmod +x ltl2smv
mvn install:install-file -Dfile="./lib/ltl2buchi.jar" -DgroupId="gov.nasa.ltl" -DartifactId=ltl2buchi -Dversion=1.0 -Dpackaging=jar;
mvn install:install-file -Dfile="./lib/rltlconv.jar" -DgroupId="de.uni_luebeck.isp" -DartifactId=rltlconv -Dversion=1.0 -Dpackaging=jar;
mvn install:install-file -Dfile="./lib/JFLAP-7.0_With_Source.jar" -DgroupId="automata" -DartifactId=JFLAP -Dversion=1.0 -Dpackaging=jar
mvn clean package
```

## Running Before
1. Download nuXmv 2.0.0 from the Internet

Download URL: https://nuxmv.fbk.eu/pmwiki.php?n=Download.Download

2. Copy the nuXmv 2.0.0 binary program to the current directory and name it `nuXmv`
```
cp [path to nuXmv-2.0.0]/nuXmv-2.0.0-linux64/bin/nuXmv ./
```

## Run Example
### PumpMine
```shell
java -jar ./target/LISFLS-run.jar -i input.txt -t 10 --swls --swlsDumpFile swlsOutput.txt
```

## Run help
```
java -jar ./target/LISFLS-run.jar -h
```

## The structure of the source code
1. LISFLS framework
```
main.BCLearner
```
2. SWON
```
localsearch.BCNeighbourhood#swlsStrategy
```
3. LISF
```
ltlsolver.LISFSolver
```

## Possible issue with building
1. java.awt.AWTError: Assistive Technology not found: org.GNOME.Accessibility.AtkWrapper

This can be done by editing the accessibility.properties file for OpenJDK:

`sudo vim /etc/java-8-openjdk/accessibility.properties`

Comment out the following line:

`assistive_technologies=org.GNOME.Accessibility.AtkWrapper`
