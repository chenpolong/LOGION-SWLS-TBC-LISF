#!/usr/bin/env python3
# 新的 LISFLS 运行脚本 script_LISFLS.py 适用于重构后的 LISFLS
# 它将原本的 script.py 和 LISFLS.sh 合二为一
# 现在在 run-full.sh 写
# python3 script_LISFLS.py - c = minepump - j = LISFLS - e = env-0 - t = 20 - -command = '--restart=20 --initialization=literal'
# 即可运行LISFLS.jar minepump 测例 重复10次，每次运行时间20秒，在env-0环境中。其中 LISFLS.jar 配置为 literal初始化，连续20次分数不变重启

# script_LISFLS.py 参数
# 必须参数：
# -c CASE, --case CASE  测例名字(与 case-studies 内文件夹名字相同)
#  -j JAR, --jar JAR     设置实验程序名字 （exec 文件夹中jar包名字，不带后缀）
#   -e ENV, --env ENV     运行的文件夹(如 env-0)
#   -t TIMEOUT, --timeout TIMEOUT
#                       运行时间
# 可选参数：
# --command COMMAND     LISFLS的其他参数（如初始化方式，是否开启local general等等）
#  -r REPETITIONS, --repetitions REPETITIONS
#                       重复运行次数(默认重复10次)
#   --start START         输出文件开始的下标编号(默认 0)

import os,re,time
import shutil
import argparse


def getCaseInput(case):
  """
  root/case-studies/case/ 中的文件，读取 'java ...' 行 并返回
  """
  cmd = ''
  #filelist = os.listdir('./case-studies/' + case)
  filelist = os.listdir('./benchmark/'+case)
  # filelist 目前只支持一个文件
  if (len(filelist) != 1):
      raise './case_studies/' + case + " 内文件数不为1个"

  suffix = filelist[0].split('.')[-1]
  if (suffix == 'sh'):
      #with open('./case-studies/' + case + '/' + filelist[0]) as file:
      with open('./benchmark/'+case+'/'+filelist[0])as file:
          for line in file:
              line = line.strip()
              if ('java' in line):
                  cmd = line
                  break
  elif (suffix == 'txt'):
      cmd = 'java -Xmx12g -Djava.library.path=/usr/local/lib -cp bin/.:lib/* main.Main' + \
          " '-i=../" + os.path.join('benchmark/', case, filelist[0]) + "'"
  return cmd



def obtainRunCommand(args):
  """
  获取 LISFLS 完成运行命令
  """
  # 获取输入例子
  cmd = getCaseInput(args.case)
  cmd = cmd.replace('-cp bin/.:lib/* main.Main', '-ea -jar ../' + args.jar + '.jar')

  # 加入超时时间
  cmd += ' -t=' + str(args.timeout)
  # 加入其他配置参数
  cmd += ' ' + args.command

  return cmd


def experiemntCore(args):
  """
  配置实验设置，重复次数，运行环境，文件下标等等
  """
  LISFLSCmd = obtainRunCommand(args)
  # 1. 进入 env 目录
  # 2. 运行 cmd，输出重定向到结果文件
  # 3. 重复运行 cmd 直到指定次数

  desDir = './output/' + args.output + '/' + args.case
  if os.path.isdir(desDir) == False:
    os.makedirs(desDir)

  # 超时退出指令
  timoutCmd = 'timeout -k ' + str(args.timeout+3) + ' ' + str(args.timeout+1) + ' '

  # 切换都 env 路径上
  desDir = '.' + desDir
  with open(args.logfile, 'a') as file:
    file.write(args.case + ': [')
    file.flush()
    for i in range(args.repetitions):
      index = i + args.start
      outputFileName = 'exec_' + str(index) + '.txt'
      shellCmd = 'cd ' + args.env + '; ' + timoutCmd + LISFLSCmd + ' > ' +   os.path.join(desDir, outputFileName) + ' 2>&1 '
      print(shellCmd)
      os.system(shellCmd)
      # print(shellCmd)
      file.write('#')
      file.flush()
    file.write(']\n')

if __name__=="__main__":
  parser = argparse.ArgumentParser(description='LISFLS 实验脚本')
  # 必填参数
  parser.add_argument('-c', '--case', required=True, type=str, help='测例名字 (与 case-studies 内文件夹名字相同)')
  parser.add_argument('-j', '--jar', required=True, type=str, help='设置实验程序名字')
  parser.add_argument('-e', '--env', required=True, type=str, help='运行的文件夹 (env-0 ... n)')
  parser.add_argument('-t', '--timeout', required=True, type=int , help='最大运行时间 (秒，默认 100)')
  parser.add_argument('-o', '--output', required=True, type=str, help='输出文件夹名称')
  # 选填参数
  parser.add_argument('--command', type=str, default='', help='LISFLS的其他参数')
  parser.add_argument('-r', '--repetitions', type=int, default=10, help='重复运行次数 (默认重复10次)')
  parser.add_argument('--start', type=int, default=0, help='输出文件开始的下标编号(默认 0)')

  args = parser.parse_args()
  #args.logfile = os.path.join(args.env, args.jar+'-'+args.case+'.log')
  #env_path = os.path.join('env',args.env)
  env_path = args.env
  if(os.path.isdir(env_path) == False):
     os.makedirs(env_path)
     os.system('cp ltl2smv '+env_path)
     os.system('cp nuXmv '+env_path)
     os.system('cp aalta_linux '+env_path)
  args.logfile = os.path.join(env_path,args.jar+'-'+args.case+'.log')
  #args.env = env_path
  with open(args.logfile, 'w') as file:
    file.write('********************** 实验信息 **********************\n')
    file.write(time.asctime(time.localtime(time.time())) + ' start\n')
    file.write('脚本名字: ' + os.path.split(__file__)[-1] + '\n')
    file.write('参数信息:' + str(args) + '\n')
    file.flush()

  experiemntCore(args)

  with open(args.logfile, 'a') as file:
    file.write(time.asctime(time.localtime(time.time())) + ' end\n')
