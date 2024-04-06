import re
from tqdm import trange
import argparse
import Levenshtein

# FILENAMES = ["AAP", "amba", "ATM", "Ele", "LAS", "LB", "LC", "MP", "PA", "roundrobin", "RP1", "RP2", "RRCS", "SA", "TCP", "Tel"]
FILENAMES = ["amba"]
APPROACH = "Tab_aalta_24"
TIMES = 10

def read_Tab(file_path:str):
    BCs = []
    with open(file_path,"r") as f:
        datas = f.readlines()
        BCs = []
        if(file_path.find("nuXmv")!=-1):
            bc_length = int(datas[0][datas[0].find("#BCs:")+5:])
        bc_length = int(datas[0][datas[0].find("#BCs: ")+6:])
        # print(file_path, len(datas), datas[0], datas[-1])
        for i in range(bc_length):
            if(file_path.find("nuXmv")!=-1):
                BCs.append(datas[2+i].replace("\n", ""))
            else:
                BCs.append(datas[3+i].replace("\n", ""))
    return BCs

def read_GA(file_path:str):
    BCs = []
    # print(file_path)
    with open(file_path,"r") as f:
        datas = f.readlines()
        BCs = []
        try:
            bc_length = int(datas[-11][11:datas[-11].find(", Total")])
        except Exception as e:
            # bc_length = int(datas[-10][11:datas[-10].find(", Total")])
            return BCs
        for i in range(bc_length):
            BCs.append(datas[-13-i].replace("\n", "").replace("(", "").replace(")", "").replace(" ", "").replace("->", "T").replace("<->", "E"))
    return BCs
if __name__=="__main__":
    for filename in FILENAMES:
        print(filename)
        BCs = []
        for ti in range(TIMES):
            path = f'./data/{filename}/{APPROACH}/exec_{ti}.txt'
            BCs = BCs + read_GA(path)
        BCs = list(set(BCs))
        leng = len(BCs)
        if(leng ==0):
            print(f'{leng} is zero')
            continue
        BC_sigmas = []
        sim_sigmas = []
        BC_deltas = []
        sim_deltas = []
        avg_min = 0
        min_distance = 10000
        # print("%BC(sigma) l 1 2 3 %\sim(sigma) l 1 2 3  %BC(delta) k 0.1 0.2 0.3 %\sim(delta) k 0.1 0.2 0.3:")
        for l in range(1, 4):
            BC_sigma = 0
            sim_sigma = 0
            for i in range(leng):
                min_distance = 10000
                count = 0
                for j in range(leng):
                    if(i==j):continue
                    lev = Levenshtein.distance(BCs[i], BCs[j])
                    if(lev< min_distance): min_distance = lev
                    if(lev <= l): count += 1
                if(count > 0): BC_sigma +=1
                sim_sigma += count
                avg_min += min_distance
            BC_sigmas.append(BC_sigma/leng)
            sim_sigmas.append(sim_sigma/leng)
        k_list = [0.1, 0.2, 0.3]
        for k in k_list:
            BC_delta = 0
            sim_delta = 0
            for i in range(leng):
                count_d = 0
                for j in range(leng):
                    if(i==j):continue
                    if(Levenshtein.distance(BCs[i], BCs[j])/(len(BCs[i])+len(BCs[j])) <= k): count_d += 1
                if(count_d > 0): BC_delta +=1
                sim_delta += count_d
            BC_deltas.append(BC_delta/leng)
            sim_deltas.append(sim_delta/leng)
        print(BC_sigmas[0], BC_sigmas[1], BC_sigmas[2], sim_sigmas[0], sim_sigmas[1], sim_sigmas[2], BC_deltas[0], BC_deltas[1], BC_deltas[2], sim_deltas[0], sim_deltas[1], sim_deltas[2], avg_min/(leng*3), leng)
            
