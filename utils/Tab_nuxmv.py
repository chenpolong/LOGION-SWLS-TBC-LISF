import os
import re
import sys
import time
import math
import json
import argparse
import subprocess
import timeout_decorator
from ltlf2dfa.parser.ltlf import LTLfParser, LTLfAnd, LTLfUntil, LTLfNot, LTLfAlways, LTLfAtomic, LTLfNext, LTLfOr, LTLfEventually, LTLfImplies, LTLfRelease

parser = argparse.ArgumentParser(description='Tab experiment')
parser.add_argument('-cf', type=str, default='../case-studies/minepump.ltl', help='input file name')
parser.add_argument('-rf', type=str, default='./output/conflicts/minepump', help='result file name')
parser.add_argument('-toS', type=int, default=5, help='time out seconds limit of LTL Solver')
args = parser.parse_args()

tempDir = "./temp"
if not os.path.isdir(tempDir):
    os.makedirs(tempDir)
def LTL2SMV(formulae:str, vocab=[f'p{i}' for i in range(100)], smv_file='./temp/1tempfile'):
    content = "MODULE main\nVAR\n"
    for v in vocab:
        content += v + ':boolean;\n'

    content += 'LTLSPEC!(\n' + formulae + ')'

    with open(smv_file, 'w') as smvfile:
        smvfile.write(content)

def nuXmv_ic3(temp_uc_path:str):

    #nuXmv_ic3求解
    cmd = './nuXmv -int'
    # -d is optional
    lines = [
        f"read_model -i {temp_uc_path}\n",
        "flatten_hierarchy\n",
        "encode_variables\n",
        "build_boolean_model\n",
        "check_ltlspec_ic3\n",
        "quit\n"
    ]
    # results = os.popen(f'{cmd}\n{lines}').readlines()
    # for result in results:
    #     if(result.readlines().find("is false") != -1):
    #         print("1")
    #         return True
    #     else:
    #         return False
    stdin_sat_solver = ''.join(lines).encode()
            #     # endwith
            # # endif
            # cur_time = time.time()
    mytask = subprocess.Popen(cmd, shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                    stderr=subprocess.PIPE)
    try:
        mytask.stdin.write((stdin_sat_solver))
        result,err = mytask.communicate(timeout= args.toS)
        if(result.decode().find("is false") != -1):
            return True
        else:
            return False
    except Exception as e:
        mytask.kill()
        return False
def Gen_Potential_bc():
    cmd = f'./main_linux {args.cf} {args.rf} "True"'
    os.system(cmd)
    # mytask = subprocess.Popen(cmd, close_fds=True, shell=True, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    # try:
    #     result, err = mytask.communicate()
    # except Exception as e:
    #     mytask.kill()
    #     print(e)
    #     return False
def findltl():
    ret = []
    filelist = os.listdir(args.rf)
    for filename in filelist:
        if(filename.endswith(".ltl")):
            ret.append(os.path.join(args.rf, filename))
    return ret
def Gen_dicts(file:str):
    dicts = {}
    token = []
    with open(file,'r') as f:
        lines = f.readlines()
    for line in lines:
        token += list(set(re.findall(r'[_a-zA-Z][_a-zA-Z0-9]*', line)))
    token = list(set(token))
    token.sort(key=lambda ele:len(ele), reverse=True)
    del_token = ['X', 'G', 'F', 'U', 'W', 'Goals', 'Domain']
    for dt in token:
        if(dt not in del_token):
            dicts[dt]=f'p{len(dicts)}'
    return dicts
def Format(dicts:dict, ltl):
    vocab = []
    replaces = {'~':'!', '||':'|', '&&':'&', '<>':'F', '[]':'G', 'W':'R', 'True':'TRUE', 'false':'FALSE', 'False':'FALSE'}
    for key in dicts:
        ltl = ltl.replace(key, dicts[key])
        vocab.append(dicts[key])
    for i in replaces:
        ltl = ltl.replace(i, replaces[i])
    return vocab, ltl
def execute():
	# 先main_linux获得潜在bc
    Time = 0
    cur_time = time.time()
    print("Computing potential Conflicts")
    Gen_Potential_bc()
    print("Computing potential Conflicts done.")
    ret = findltl()
    conflicts = []
    
    for file in ret:
        out = f'{file}.out'
        out_data = []
        ok = True
        flag = True
        Time += float(time.time() - cur_time)
        dicts = Gen_dicts(file)
        cur_time = time.time()
        with open(file, 'r') as f:
            ltls = f.readlines()
        print("Filtering: ", ltls[0])
        for i in range(1, len(ltls)):
            ltl = ltls[i]
            Time += float(time.time() - cur_time)
            vocab, ltl = Format(dicts, ltl)
            LTL2SMV(ltl, vocab=vocab, smv_file='./temp/1tempfile')
            cur_time = time.time()
            if(flag):
                if(nuXmv_ic3('./temp/1tempfile')):
                    ok = False
                    out_data.append(f"Fomulae {i}: satisfiable\n")
                    break
                else:
                    out_data.append(f"Fomulae {i}: unsatisfiable\n")
                    flag = False
            else:
                if(not nuXmv_ic3('./temp/1tempfile')):
                    ok = False
                    out_data.append(f"Fomulae {i}: unsatisfiable\n")
                    break
                else:
                    out_data.append(f"Fomulae {i}: satisfiable\n")
            # print(float(time.time()-cur_time))
        if(ok):
            conflicts.append(ltl)
        with open(out, 'w') as f:
            for ele in out_data:
                f.write(ele)
    # 将获得的全部.ltl（每个文件包含一个潜在bc条件）逐个判断是否满足inconsistency和minimality（调用时间限制为args.toS的nuXmv-ic3求解）
    results = f'{args.rf}/result.txt'
    results_data = [f'#BCs:{len(conflicts)}\n', 'Computed BCs:\n']
    for ele in conflicts:
        results_data.append(ele)
    results_data.append(f'TOTAL execution time (secs) {Time}s')
    with open(results,'w') as f:
        for ele in results_data:
            f.write(ele)
    # 读写bc
if __name__ == "__main__":
    execute()
