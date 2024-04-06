from execute_nuxmv import LTL2SMV, nuXmv_ic3
import re
import os
from tqdm import trange
from multiprocessing import Pool

def Parse(ltl:str):
    vocab = []
    token = list(set(re.findall(r'[_a-zA-Z][_a-zA-Z0-9]*', ltl)))
    del_token = ['X', 'G', 'F', 'U', 'W']
    for dt in token:
        if(dt not in del_token):
            vocab.append(dt)
    replaces = {'~':'!', '||':'|', '&&':'&', '<>':'F', '[]':'G', 'W':'R', 'True':'TRUE', 'false':'FALSE', 'False':'FALSE'}
    for i in replaces:
        ltl = ltl.replace(i, replaces[i])
    return vocab, ltl

def Gen_BCs(BCs:list):
    gen_BCs = []
    for i in trange(len(BCs)):
        bc1 = BCs[i]
        flag = True
        vocab1, bc1 = Parse(bc1)
        for bc2 in BCs:
            if(bc1==bc2):continue
            vocab2, bc2 = Parse(bc2)
            vocab = list(set(vocab1+vocab2))
            ltl1 = f'({bc1}) -> ({bc2})'
            ltl2 = f'({bc2}) -> ({bc1})'
            temp1 = f'./temp/{ltl1.replace(" ", "")}'
            temp2 = f'./temp/{ltl2.replace(" ", "")}'
            LTL2SMV(ltl1, vocab, temp1)
            LTL2SMV(ltl2, vocab, temp2)
            if(not nuXmv_ic3(temp2) and nuXmv_ic3(temp1)):
                flag = False
                break
        if(flag):
            gen_BCs.append(bc1)
    return gen_BCs
def multi_gen_BCs(BCs:list, process_num:int):
    leng = int(len(BCs)/process_num)
    result = []
    pool = Pool(process_num)
    for i in range(process_num):
        if(leng*(i+1) >len(BCs)):
            result.append(pool.apply_async(Gen_BCs, args=(BCs[leng*i:],)))
        else:
            result.append(pool.apply_async(Gen_BCs, args=(BCs[leng*i:leng*(i+1)],)))
    pool.close()
    pool.join