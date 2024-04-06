package main;

import gov.nasa.ltl.trans.ParseErrorException;
import localgeneral.LocalGeneralDefaultImp;
import localsearch.*;
import ltlparse.Formula;
import ltlparse.Parser;
import ltlsolver.LTLSolverFactory;
import modelcounting.ModelCounterFactory;
import refine.Refinement;
import utils.ParserUtils;
import utils.Polling;

import java.io.IOException;
import java.util.*;

/**
 * 描述 算法运行时 状态，请通过 {@link #initalization(List, List) 中修改}
 */
public class LogionState {
    /**
     * 算法是否停止
     */
    static public boolean searchStop = false;

    /**
     * 算法是否要重启
     */
    static public boolean searchRestart = false;

    /**
     * 全局统一随机数生成器
     */
    static public final Random random = new Random();

    /**
     * 全局唯一 BCObjective
     */
    static public BCObjective bcObjective;

    /**
     * 全局唯一 BCData
     */
    static public BCData bcData;

    /**
     * 全局唯一 LTLSolverFactory
     */
    static public LTLSolverFactory ltlSolverFactory;

    /**
     * 全局唯一 SolutionCollector
     */
    static public SolutionCollector solutionCollector;

    /**
     * model counter
     */
    static public ModelCounterFactory modelCounterFactory;


    /**
     * 全局唯一 BCNeighbourhood
     */
    static public BCNeighbourhood bcNeighbourhood;

    /**
     * 全局唯一 TabuMemory
     */
    static public BCTabuMemory bcTabuMemory;

    /**
     * 全局唯一 ProgressSearchListener
     */
    static public ProgressSearchListener progressSearchListener;

    /**
     * 全局唯一 的 Local general 方法
     */
    static public LocalGeneralDefaultImp localGeneralImp = new LocalGeneralDefaultImp();

    /**
     * 全局唯一的 重启策略函数
     */
    static public LocalSearchRestart localSearchRestart;

    /**
     * 问题的 doms
     */
    static public List<String> doms;
    static public List<String> goals;
    static public List<String> vars;


    static public List<Formula<String>> formulaDoms = new ArrayList<>();
    static public List<Formula<String>> formulaGoals = new ArrayList<>();

    /**
     * 开始搜索时间 System.currentTimeMillis();
     */
    static public long startTime;

    static public boolean stopByTime = true;


    /**
     * model counter 是否能超时时间 {@link InitialConfiguration#modelCountingTimeout} 内计算出模型
     */
    static public boolean canModelCounting = true;

    static public Refinement refinement = null;

    /**
     * 负责初始化本类中的静态变量
     * @param doms
     * @param goals
     * @throws ParseErrorException
     */
    public static void initalization(List<String> doms, List<String> goals) throws ParseErrorException, IOException, InterruptedException {
        /**
         * 记录开始时间
         */
        LogionState.startTime = System.currentTimeMillis();

        for (String str : doms) {
            formulaDoms.add(Parser.parse(str));
        }
        for (String str : goals) {
            formulaGoals.add(Parser.parse(str));
        }

        LogionState.doms = doms;
        LogionState.goals = goals;

        /**
         * 初始化统计类
         */
        SolutionCollector solutionCollector = new SolutionCollector();
        LogionState.solutionCollector = solutionCollector;

        /**
         * 生成工厂类 LTLSolverFactory
         */
        LTLSolverFactory solverFactory = new LTLSolverFactory(formulaDoms,formulaGoals);
        LogionState.ltlSolverFactory = solverFactory;


        /**
         * 生成问题的数据集合 BCData
         */
        Set<String> vars_set = ParserUtils.getVariables(doms, goals);
        vars = new LinkedList<>(vars_set);
        Set<Formula<String>> formulaVars = new LinkedHashSet<>();
        for (String var : vars_set) {
            formulaVars.add(Parser.parse(var));
        }
        BCData data = new BCData(new ArrayList<>(formulaVars));
        LogionState.bcData = data;

        /**
         * 生成 model counting
         */
        if (InitialConfiguration.usingModelCounting) {
            modelCounterFactory = new ModelCounterFactory();
            modelCounterFactory.initialize(InitialConfiguration.modelCountingMode);
        }

        /**
         * 生成目标函数 BCObjective
         */
        BCObjective obj = new BCObjective(formulaDoms, formulaGoals);
        LogionState.bcObjective = obj;

        /**
         * 生成 local search 邻居定义 BCNeighbourhood
         */
        BCNeighbourhood neighbourhood = new BCNeighbourhood(data, obj);
        LogionState.bcNeighbourhood = neighbourhood;

        /**
         * 生成禁忌表
         */
        BCTabuMemory tabuMemory = new BCTabuMemory();
        LogionState.bcTabuMemory = tabuMemory;

        /**
         * 生成local search 监听器
         */
        LogionState.progressSearchListener = new ProgressSearchListener();

        /**
         * 生成重启策略函数
         */
        Polling<Formula<String>> initialSolutionDet = LocalSearchInitialization.initialization(formulaDoms, formulaGoals, InitialConfiguration.initializationPolicy);
        localSearchRestart = new LocalSearchRestartDefaultImp(initialSolutionDet);

        if(InitialConfiguration.usingRefine){
            refinement = new Refinement();
        }
    }
}
