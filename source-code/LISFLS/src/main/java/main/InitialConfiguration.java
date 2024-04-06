package main;

import localsearch.InitializationType;
import ltlsolver.LTLSolverType;
import modelcounting.ModelCounterType;
import org.apache.commons.cli.CommandLine;
import refine.goalimproving.GoalImproverType;

import java.io.File;
import java.io.IOException;

/**
 * 描述 算法 初始化的配置信息， 通过修改这些初始化的配置可以改变算法行为
 * 需要在 入口函数 地方初始化这些配置, 否则使用默认配置
 * 存放只读变量，变量的修改通过 {@link #parseCommandLine(CommandLine)} 完成
 */
public class InitialConfiguration {
    /**
     * nuXmv 存储 临时model文件的文件夹地址, 每次启动算法都会清空 modelDir 文件夹
     */
    static public String modelDir = "./model/";

    /**
     * nuXmv 程序路径
     */
    static public String nuXmv = "./nuXmv";

    /**
     * aalta 程序路径
     */
    static public String aalta = "./aalta_linux";

    /**
     * ptlt 程序路径
     */
    static public String pltl = "./pltl";

    /**
     * 存储临时文件的路径 (用于存放 lasso/path)
     */
    static public String tempDir = "./temp/";

    /**
     * ltl2smv 程序
     */
    static public String ltl2smv = "./ltl2smv";

    /**
     * 禁忌表大小 Tabu Memory
     */
    static public int tabuMemorySize = 4;

    /**
     * 公式编辑操作权重
     */
    static public double renameWeight       = 1;
    static public double insertWeight       = 1;
    static public double deleteWeight       = 1;

    /**
     * LTL公式操作符权重
     */
    // Unary Operator
    static public double notWeight          = 1;
    static public double nextWeight         = 1;
    static public double globallyWeight     = 1;
    static public double futureWeight       = 1;
    // Binary Operator
    static public double andWeight          = 1;
    static public double orWeight           = 1;
    static public double iffWeight          = 1;
    static public double untilWeight        = 1;
    static public double weakUntilWeight    = 1;

    /**
     * 是否开启 local general
     */
    static public boolean localGeneral = false;

    /**
     * 有界 K 邻居选择策略 K 值
     */
    static public int  neighbourhoodsKsize = 50;

    /**
     * 评分函数的分数设置
     */
    static public double objectiveValueInconsistency = 1;
    static public double objectiveValueMinimality = 1;
    static public double objectiveValueNontriviality = 0.5;
    static public double objectiveValueMinScore = objectiveValueInconsistency + objectiveValueMinimality + objectiveValueNontriviality;

    /**
     * 使用的LTL SAT 判定器
     */
    static public LTLSolverType solverType = LTLSolverType.LISF;

    /**
     * 单次 LTL SAT 判定最长时间(秒)
     */
    static public long ltlCheckTimeout = 5;

    /**
     * LISF 参数
     * {@link #LISFmaxLasso} 每个子属性最大保存的 lasso 数量
     * {@link #LISFdepreciationRate} lasso 分数的衰减速度
     * {@link #LISFModelCheckAPathTimeout} model check a path 的最大时间
     * {@link #LISFlassoinitialValue} lasso 的初始分数, 但lasso分数小于 初始分数一半时, 被删除
     */
    static public int LISFmaxLasso = 5;
    static public double LISFdepreciationRate = 0.95;
    static public long LISFModelCheckAPathTimeout = 2;
    static public double LISFlassoinitialValue = 1.0;

    /**
     * local search 初始化策略
     */
    static public InitializationType initializationPolicy = InitializationType.trivialBC;

    /**
     * local search 最大求解时间 秒
     */
    static public long localSearchTimeout = 100;

    /**
     * 重启条件：连续 K 次目标函数分数不变
     */
    static public boolean usingRestart = false;
    static public int restartCondition = 20;

    /**
     * 是否使用 模型计数 评估BC发生的可能性
     */
    static public boolean usingModelCounting = false;

    /**
     * 模型计数的边界
     */
    static public int modelCountingBound = 1000;

    /**
     * 单次模型计数的最大时间 秒
     */
    static public int modelCountingTimeout = 10;

    /**
     * 模型计数类型
     */
    static public ModelCounterType modelCountingMode = ModelCounterType.CACHE;

    static public Boolean usingRefine = false;
    static public GoalImproverType goalImproverType = GoalImproverType.SIMPLE;
    static public int modifyMaxTime = 5;
    static public int refineTimeout = 20;

    static public boolean usingCore = false;

    /**
     * 是否开启 weakenBC 的选项
     */
    static public boolean weakenBC = false;

    /**
     * 是否使用 swls 算法
     */
    static public boolean swlsFlag = false;
    /**
     * SWLS(strengthen and weaken local search) 算法中
     * 如果不满足M：以概率swlsWeakenProbability：进行 weaken，得到公式 Formula；其他：放弃它
     * 如果不满足LI：以概率swlsStrengthenProbability：进行strengthen，得到公式 Formula；其他：放弃它
     */
    static public double swlsWeakenProbability = 0.1;
    static public double swlsStrengthenProbability = 0.1;

    /**
     * 将 SWLS 计算的公式输出到指定文件中, 输出文件格式
     * #swlsStrengthenFormulae
     * 原公式1, 强化后公式1
     * 原公式2, 强化后公式2
     * ...
     * #swlsWeakenFormulae
     * 原公式n, 弱化后公式n
     * ...
     */
    static public String swlsDumpFile = null;

    /**
     * 通过命令行参数初始化上面的参数配置
     * @param commandLine
     */
    static void parseCommandLine(CommandLine commandLine) {
        if (commandLine.hasOption("t")) {
            String str = commandLine.getOptionValue("t");
            localSearchTimeout = Integer.parseInt(str);
        }
        if (commandLine.hasOption("s")) {
            String str = commandLine.getOptionValue("s");
            solverType = LTLSolverType.valueOf(str);
        }
        if (commandLine.hasOption("ltlcheckmaxtime")) {
            String str = commandLine.getOptionValue("ltlcheckmaxtime");
            ltlCheckTimeout = Integer.parseInt(str);
        }
        if (commandLine.hasOption("initialization")) {
            String str = commandLine.getOptionValue("initialization");
            initializationPolicy = InitializationType.valueOf(str);
        }
        if (commandLine.hasOption("localgeneral")) {
//            String str = commandLine.getOptionValue("localgeneral");
//            localGeneral = Boolean.parseBoolean(str);
            localGeneral = true;
        }
        if (commandLine.hasOption("restart")) {
            String str = commandLine.getOptionValue("restart");
            usingRestart = true;
            restartCondition = Integer.parseInt(str);
        }
        if (commandLine.hasOption("neighbourhoodsKsize")) {
            String str = commandLine.getOptionValue("neighbourhoodsKsize");
            neighbourhoodsKsize = Integer.parseInt(str);
        }
        if (commandLine.hasOption("MC")) {
            String str = commandLine.getOptionValue("MC");
            usingModelCounting = true;
            if(str.equals("Cache")){
                modelCountingMode = ModelCounterType.CACHE;
                modelCountingBound = 30;
            }
            else if(str.equals("Likelyhood")){
                modelCountingMode = ModelCounterType.LIKELYHOOD;
                modelCountingBound = 10;
            }
            else if(str.equals("Approximate")){
                modelCountingMode = ModelCounterType.APPROXIMATE;
                modelCountingBound = 1000;
            }
        }
        if (commandLine.hasOption("modelCountingBound")) {
            String str = commandLine.getOptionValue("modelCountingBound");
            modelCountingBound = Integer.parseInt(str);
        }
        if (commandLine.hasOption("modelCountingTimeout")) {
            String str = commandLine.getOptionValue("modelCountingTimeout");
            modelCountingTimeout = Integer.parseInt(str);
        }
        if(commandLine.hasOption("refine")){
            usingRefine = true;
            String str = commandLine.getOptionValue("refine");
            if(str.equals("simple")){
                goalImproverType = GoalImproverType.SIMPLE;
            }
            else if(str.equals("weaken")){
                goalImproverType = GoalImproverType.WEAKEN;
            }
            else if(str.equals("strengthen")){
                goalImproverType = GoalImproverType.STRENGTHEN;
            }
        }
        if(commandLine.hasOption("core")){
            usingCore = true;
        }
        if (commandLine.hasOption("weakeningBC")) {
            weakenBC = true;
        }
        if (commandLine.hasOption("swls")) {
            swlsFlag = true;
        }
        if (commandLine.hasOption("swlsWeaken")) {
            String str = commandLine.getOptionValue("swlsWeaken");
            swlsWeakenProbability = Double.parseDouble(str);
        }
        if (commandLine.hasOption("swlsStrengthen")) {
            String str = commandLine.getOptionValue("swlsStrengthen");
            swlsStrengthenProbability = Double.parseDouble(str);
        }
        if (commandLine.hasOption("swlsDumpFile")) {
            String str = commandLine.getOptionValue("swlsDumpFile");
            File dumpFile = new File(str);
            try {
                dumpFile.createNewFile();
                if (!dumpFile.canWrite()) {
                    throw new IllegalArgumentException("swlsDumpFile: Invalid parameter, " + str + " cannot be written");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("swlsDumpFile: Invalid parameter, " + str + " cannot be written", e);
            }
            swlsDumpFile = str;
        }
    }

    static void printInitialConfiguration() {

        System.out.println("*************************************** Configuration **************************************");
        /***
         * local search 相关
         */
        System.out.println("【BASIC SETTINGS】");
        System.out.println("        max solvtion time: " + localSearchTimeout + " s");
        System.out.println("        objective function value: inconsistency(" + objectiveValueInconsistency + "), " +
                "minimality(" + objectiveValueMinimality + "), " +
                "non-triviality(" + objectiveValueNontriviality + ")");
        System.out.println("        Min Solution value: " + objectiveValueMinScore);
        System.out.println("        tabu memory size: " + tabuMemorySize);
        System.out.println("        neighbourhoodsKsize: " + neighbourhoodsKsize);
        /**
         * LTL 求解器相关
         */
        System.out.println("【SAT CHECKER SETTINGS】");
        System.out.println("        LTL Satisfiability Check: " + solverType);
        System.out.println("        ltl check max time: " + ltlCheckTimeout);
        if (solverType == LTLSolverType.LISF) {
            System.out.println("        counter example model checking a path MAX time: " + LISFModelCheckAPathTimeout);
            System.out.println("        counter example model checking MAX time: " + ltlCheckTimeout);
            System.out.println("        counter example MAX traces: " + LISFmaxLasso);
            System.out.println("        counter example depreciationRate: " + LISFdepreciationRate);
        }
        /**
         * 初始化方法
         */
        System.out.println("【INITIALIZATION SETTINGS】");
        System.out.println("        initialize method: " + initializationPolicy);
        /**
         * Local general策略
         */
        System.out.println("【LOCAL GENERAL SETTINGS】");
        System.out.println("        local general: " + localGeneral);
        /**
         * 模型计数相关
         */
        System.out.println("【MODEL COUNTING SETTINGS】");
        System.out.println("        Enable model counting Strategy: " + usingModelCounting);
        if (usingModelCounting) {
            System.out.println("        ModelCounting mode: " + InitialConfiguration.modelCountingMode);
            System.out.println("        ModelCounting max time: " + InitialConfiguration.modelCountingTimeout);
            System.out.println("        ModelCounting bound: " + InitialConfiguration.modelCountingBound);
        }
        /**
         * 重启策略
         */
        System.out.println("【RESTART SETTINGS】");
        System.out.println("        Enable restart strategy: " + usingRestart);
        if(usingRestart){
            System.out.println("        restart condition: " + restartCondition);
        }
        /**
         * 操作符权重设置
         */
        System.out.println("【OP WEIGHT SETTINGS】");
        System.out.println("        Weight: rename(" + String.format("%.2f", renameWeight) + "), " +
                "insertion(" + String.format("%.2f", insertWeight) + "), " +
                "deletion(" + String.format("%.2f", deleteWeight) + ")"
        );
        System.out.println("        Weight: not(" + String.format("%.2f", notWeight) + "), " +
                "globally(" + String.format("%.2f", globallyWeight) + "), " +
                "future(" + String.format("%.2f", futureWeight) + "), " +
                "and(" + String.format("%.2f", andWeight) + "), " +
                "or(" + String.format("%.2f", orWeight) + "), " +
                "iff(" + String.format("%.2f", iffWeight) + "), " +
                "until(" + String.format("%.2f", untilWeight) + "), " +
                "weak_until(" + String.format("%.2f", weakUntilWeight) + ")"
        );
        /**
         * refine module
         */
        System.out.println("【REFINE SETTINGS】");
        System.out.println("        Enable refine module: " + usingRefine);
        if(usingRefine){
            System.out.println("        max modify trying for a goal: " + modifyMaxTime);
            System.out.println("        total time for a refine: " + refineTimeout);
        }
        /**
         * coreFinding module
         */
        System.out.println("【CORE SETTINGS】");
        System.out.println("        Enable core plugin: " + usingCore);

        /**
         * weakening process
         */
        System.out.println("【WEAKENING BC PROCESS】");
        System.out.println("        Enable weakening BC process: " + weakenBC);

        System.out.println("【SWLS SETTINGS】");
        System.out.println("        Enable SWLS: " + swlsFlag);
        if (swlsFlag) {
            System.out.println("        strengthen probability: " + swlsStrengthenProbability);
            System.out.println("        weaken probability: " + swlsWeakenProbability);
            System.out.println("        dump file: " +
                    (swlsDumpFile != null ? new File(swlsDumpFile).getAbsolutePath() : "null"));
        }

        System.out.println("\n********************************************************************************************");
    }
}
