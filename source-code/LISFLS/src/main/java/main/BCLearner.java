package main;

import gov.nasa.ltl.trans.ParseErrorException;
import localsearch.*;
import ltlparse.Formula;
import ltlparse.Parser;
import ltlsolver.LISFSolver;
import ltlsolver.LTLSolverType;
import org.jamesframework.core.exceptions.SearchException;
import org.jamesframework.core.problems.GenericProblem;
import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.sol.RandomSolutionGenerator;
import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.algo.tabu.TabuSearch;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import utils.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BCLearner {
    public void learnBC(List<String> doms, List<String> goals) throws ParseErrorException {
        List<Formula<String>> formulaDoms = new ArrayList<>();
        List<Formula<String>> formulaGoals = new ArrayList<>();
        for (String str : doms) {
            formulaDoms.add(Parser.parse(str));
        }
        for (String str : goals) {
            formulaGoals.add(Parser.parse(str));
        }

        RandomSolutionGenerator<BCSolution, BCData> rsg =  (r, d) -> {
            // 禁止随机生成解
            throw new UnsupportedOperationException("Creating a random potential BC is not supported.");
        };

        /**
         * 生成 local search 问题
         */
        Problem<BCSolution> problem = new GenericProblem<>(LogionState.bcData, LogionState.bcObjective, rsg);

        /**
         * 使用 禁忌搜索
         */
        LocalSearch<BCSolution> localSearch = new TabuSearch<>(problem, LogionState.bcNeighbourhood, LogionState.bcTabuMemory);

        /**
         * 设置随机数生成器
         */
        localSearch.setRandom(LogionState.random);

        /**
         * 监控线程, 保证按时结束程序
         */
        Thread daemon = new Thread(() -> {
            try {
                Thread.sleep(InitialConfiguration.localSearchTimeout*1000);
                Signal.raise(new Signal("TERM"));
            } catch (InterruptedException e) {
            }
        });
        daemon.start();

        /**
         * signal
         */
        SignalHandler killHandler = signal -> {
            String name = signal.getName();
            int number = signal.getNumber();
            String currentThreadName = Thread.currentThread().getName();

            LogionState.searchStop = true;
            localSearch.stop();
            daemon.interrupt();
            System.out.println("[Thread:"+currentThreadName + "] receved signal: " + name + ", number: " + number);
        };
        Signal.handle(new Signal("TERM"), killHandler);
        Signal.handle(new Signal("INT"), killHandler);

        /**
         * 打印输入和配置信息
         */
        System.out.println("Domain: " + String.join(", ", doms));
        System.out.println("Goals: " + String.join(", ", goals));
        InitialConfiguration.printInitialConfiguration();

        /**
         * 设置监听器
         */
        localSearch.addSearchListener(LogionState.progressSearchListener);

        /**
         * 设置初始解
         */
        BCSolution initialSolution = LogionState.localSearchRestart.nextInitialSolution();
        Evaluation value = LogionState.bcObjective.evaluate(initialSolution, LogionState.bcData);
        System.out.println("Init Value: " + String.format("%.2f", value.getValue()) +
                ", Init Formula: " + initialSolution.getFormula().toPLTLString());
        localSearch.setCurrentSolution(initialSolution);

        /**
         * 设置搜索终止条件
         * 剩余的时间 restTime 毫秒单位
         */
        StatisticState.initialTime = System.currentTimeMillis() - LogionState.startTime;
        long restTime = InitialConfiguration.localSearchTimeout*1000 - StatisticState.initialTime;
        if (restTime <= 0) { restTime = 1; }
        MaxRuntime maxRuntime = new MaxRuntime(restTime, TimeUnit.MILLISECONDS);
        localSearch.addStopCriterion(maxRuntime);
        /**
         * 设置重启监听器
         */
        localSearch.addStopCriterion(search -> LogionState.searchRestart);

        /**
         * 开始搜索
         */
        while (!LogionState.searchStop) {
            localSearch.start();
            if (LogionState.searchRestart) {
                LogionState.searchRestart = false;
                System.out.println(" >>> local search restart !!!");
                BCSolution restartSolution = LogionState.localSearchRestart.nextInitialSolution();
                Evaluation restartValue = LogionState.bcObjective.evaluate(restartSolution, LogionState.bcData);
                System.out.println("Restart Value: " + String.format("%.2f", restartValue.getValue()) +
                        ", Restart Formula: " + restartSolution.getFormula().toPLTLString());
                localSearch.setCurrentSolution(restartSolution);
                /**
                 * 重新设置剩余超时时间
                 */
                long restartRestTime = InitialConfiguration.localSearchTimeout*1000 - (System.currentTimeMillis() - LogionState.startTime);
                if (restartRestTime <= 0) { restartRestTime = 1; }
                localSearch.removeStopCriterion(maxRuntime);
                maxRuntime = new MaxRuntime(restartRestTime, TimeUnit.MILLISECONDS);
                localSearch.addStopCriterion(maxRuntime);
            }
        }

        /**
         * 打印结果和统计信息
         */
        printResultsAndStatistics();
    }

    void printResultsAndStatistics() {
        System.out.println();
        System.out.println("**************************************** Solution ****************************************");
        SolutionCollector solutionCollector = LogionState.solutionCollector;
        List<BCInfo> solutions = solutionCollector.getSolutions();
        for (BCInfo bcInfo : solutions) {
            System.out.println("value: " + String.format("%.2f", bcInfo.getValue()) +
                    ", time: " + String.format("%.2f", bcInfo.getTime()) +
                    ", regularization: " + bcInfo.getRegularValue() +
                    ", bc: " + bcInfo.getBc() +
                    (InitialConfiguration.localGeneral ? (", localgeneral: " + bcInfo.getLocalGeneralValue()) : "") +
                    (InitialConfiguration.usingModelCounting ? (", modelpercent: " + bcInfo.getModelCounting()) : "") );
        }
        System.out.println();

        if (InitialConfiguration.localGeneral) {
            System.out.println("************************************** General BC ****************************************");
            for (String bc : LogionState.localGeneralImp.getGeneralBC()) {
                System.out.println(bc);
            }
        }

        System.out.println("************************************** Best Formula **************************************");
        if (!solutions.isEmpty()) {
            BCInfo bestBCInfo = LogionState.solutionCollector.bestBC();
            System.out.println("Best formula: " + bestBCInfo.getBc());
            System.out.println("The fitness value of best formula: " + String.format("%.2f", bestBCInfo.getValue()));
        } else {
            System.out.println("No valid solution found...");
        }
        System.out.println("*******************************************************************************************");

        System.out.println("**************************************** weakened BC pair ***************************************");
        for (Pair<BCSolution, BCSolution> pair : StatisticState.weakenBCPair) {
            System.out.println(pair.getKey().getFormula() + ", " + pair.getValue().getFormula());
        }

        System.out.println("**************************************** Statistics ***************************************");
        StatisticState.printStatisticState();
        if (InitialConfiguration.usingModelCounting) {
            System.out.println("can model counting: " + LogionState.canModelCounting);
        }

        if (InitialConfiguration.solverType == LTLSolverType.LISF) {
            LISFSolver lisfSolver = (LISFSolver)LogionState.ltlSolverFactory.getSolver(LTLSolverType.LISF);
            lisfSolver.dropout();
        }
        if(InitialConfiguration.usingRefine){
            LogionState.refinement.printRefinement();
        }
        System.out.println();
        /**
         * 判断输出是否含有重复解
         */
        Set<String> bcs = new HashSet<>();
        for (BCInfo bcinfo : solutions) {
            bcs.add(bcinfo.getBc());
        }
        if (bcs.size() != solutions.size()) {
            throw new SearchException("输出的解从含有重复解");
        }

    }
}
