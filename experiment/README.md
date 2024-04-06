# Experiment of LISFLS
The experiment for `Lasso-based Incremental Satisfiability Filter for Local Searching More General Boundary Conditions in Requirements Specification`

## Content
1. Executables of LISFLS
2. benchmark data

## Running Before
1. Download nuXmv 2.0.0 from the Internet

Download URL: https://nuxmv.fbk.eu/pmwiki.php?n=Download.Download

2. Copy the nuXmv 2.0.0 binary program to the current directory and name it `nuXmv`
```
cp [path to nuXmv-2.0.0]/nuXmv-2.0.0-linux64/bin/nuXmv ./
```

## Different settings of LISFLS
- LISFLS-Aa:       java -jar LISFLS.jar --swls --swlsDumpFile swlslog.txt -s aalta -t 3600
- LISFLS-nu:       java -jar LISFLS.jar --swls --swlsDumpFile swlslog.txt -s nuXmv -t 3600
- LISFLS:          java -jar LISFLS.jar --swls --swlsDumpFile swlslog.txt -s LISF  -t 3600
- LISFLS-simp-Aa: java -jar LISFLS.jar -s aalta --initialization literal -t 3600
- LISFLS-simp:    java -jar LISFLS.jar -s LISF  --initialization literal -t 3600

# Experiment of benchmark data
0. Add run permissions
```
chmod +x aalta_linux
chmod +x ltl2smv
chmod +x nuXmv
```

1. We provide a script ‘script_LISFLS.py’ to run the experiment easily

usage: script_LISFLS.py [-h] -c CASE -j JAR -e ENV -t TIMEOUT -o OUTPUT [--command COMMAND] [-r REPETITIONS] [--start START]

2. Run MinePump by LISFLS
```shell
python3 script_LISFLS.py -r=1 -c=minepump -j=LISFLS -o=LISFLS -t=3600 -e=env-0 --command='--swls --swlsDumpFile ../output/LISFLS/minepump/exec_0.swls -s LISF'
```
Command will run the 3600s, results will save in the ./output/LISFLS/minepump/exec_0.txt

3. Run RRCS by LISFLS-Aa
```shell
python3 script_LISFLS.py -r=1 -c=rrcs -j=LISFLS -o=LISFLS-Aa -t=3600 -e=env-0 --command='--swls --swlsDumpFile ../output/LISFLS-Aa/rrcs/exec_0.swls -s aalta'
```
Command will run the 3600s, results will save in the ./output/LISFLS-Aa/rrcs/exec_0.txt
