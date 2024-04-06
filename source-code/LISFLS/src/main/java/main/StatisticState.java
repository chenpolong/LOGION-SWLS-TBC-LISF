package main;

import localsearch.BCSolution;
import ltlparse.Formula;
import utils.IOUtils;
import utils.Pair;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class StatisticState {
    /**
     * 调用评分函数的次数(包含重复公式)
     */
    static public int callEvaluateCount = 0;

    /**
     * 调用评分函数的次数(不包含重复公式)
     */
    static public int callEvaluateDistinctCount = 0;

    /**
     * 搜索到BC的次数(包含重复BC)
     */
    static public int findBCCount = 0;

    /**
     * 搜索到BC的次数(不包含重复BC)
     */
    static public int findBCDistinceCount = 0;

    /**
     * 调用评分函数花费的时间
     */
    static public long callEvaluateTime = 0;

    /**
     * 判定 重言式 的个数
     */
    static public int findTautology = 0;

    /**
     * 判定 永假式 的个数
     */
    static public int findContradictory = 0;

    /**
     * 调用 LTL solver 的次数
     */
    static public int callSolverCount = 0;

    /**
     * 调用 LTL solver 超时次数
     */
    static public int callSolverTimeoutCount = 0;

    /**
     * 调用 LTL solver 错误次数
     */
    static public int callSolverErrorCount = 0;

    /**
     * 调用 LTL sovler 的时间
     */
    static public long callSolverTime = 0;

    /**
     * 禁忌表禁忌的次数
     */
    static public int tabuMemoryRejectCount = 0;

    /**
     * local general 时间
     */
    static public long localGeneralTime = 0;

    /**
     * 检查 logical inconsistency 时间
     */
    static public long logicalInconsistencyTime = 0;

    /**
     * 检查 minimality
     */
    static public long minimalityTime = 0;

    /**
     * 检查 non-triviality
     */
    static public long nonTrivialityTime = 0;

    /**
     * 直接判定 LTL 可满足性 (非BC的性质)
     */
    static public long ltlsatTime = 0;

    /**
     * 重启的次数
     */
    static public int restartCount = 0;

    /**
     * 模型计数启动时间
     */
    static public long modelCounterStartTime = 0;

    /**
     * 模型计数时间
     */
    static public long callModelCounterTime = 0;

    /**
     * 调用模型计数的次数
     */
    static public int callModelCounterCount = 0;
    /**
     * 调用模型计数的次数
     */
    static public int modelCounterZeroCount = 0;

    /**
     * 初始化时间
     */
    static public long initialTime = 0;

    /**
     * 通过 weakening 过程找到 weakenBC 的个数
     * 数组每一项保存 (weakening前的BC， weakening后的BC)
     */
    static public List<Pair<BCSolution, BCSolution>> weakenBCPair = new LinkedList<>();

    /**
     * weakening 过程 消耗的时间
     */
    static public long weakenBCTime = 0;

    /**
     * swls 消耗的时间
     */
    static public long swlsWeakenTime = 0;
    static public long swlsStrengthenTime = 0;

    /**
     * 調用 swls 的調用次數
     */
    static public long swlsWeakenCount = 0;
    static public long swlsStrengthenCount = 0;

    /**
     * 通过 swls 找到的具体合适的公式
     */
    static public List<Pair<Formula<String>, Formula<String>>> swlsWeakenFormulae = new LinkedList<>();
    static public List<Pair<Formula<String>, Formula<String>>> swlsStrengthenFormulae = new LinkedList<>();

    /**
     * swls 中 不满足NT：直接加入 的公式个数
     */
    static public int swlsTrivialFormula = 0;

    /**
     * 调用 swls 算法的次数
     */
    static public int swlsStrategyCount = 0;

    /**
     * 调用 swls 算法消耗的时间
     */
    static public long swlsStrategyTime = 0;

    static public void printStatisticState() {
        /**
         * local search 相关的统计信息
         */
        System.out.println("【TIME INFO】");
        System.out.println("        Initial time: " + String.format("%.2f", (initialTime/1000.0)));
        System.out.println("        Total Time (without filtering unique solutions time): "+ String.format("%.2f", (System.currentTimeMillis()-LogionState.startTime)/ 1000.0) +
                " sec, Evaluate time: " + String.format("%.2f", callEvaluateTime/1000.0) + " sec");
        System.out.println("        Stoped by time counter: "+LogionState.stopByTime);
        System.out.println("        logical inconsistency time: " + String.format("%.2f", StatisticState.logicalInconsistencyTime/1000.0) +
                ", minimality time: " + String.format("%.2f", StatisticState.minimalityTime/1000.0) +
                ", non-triviality time: " + String.format("%.2f", StatisticState.nonTrivialityTime/1000.0) +
                ", time to call solver directly: " + String.format("%.2f", StatisticState.ltlsatTime/1000.0));

        System.out.println("【SEARCH INFO】");
        System.out.println("        Tautology: " + findTautology + ", Contradictory formula: " + findContradictory);
        System.out.println("        REEVALUATIONS: "+ (StatisticState.callEvaluateCount - StatisticState.callEvaluateDistinctCount)
                + " OF " + StatisticState.callEvaluateCount);
        System.out.println("        Search formula: " + StatisticState.callEvaluateDistinctCount);
        System.out.println("        Total solution: " + StatisticState.findBCDistinceCount + ", Solution(with duplicate solution): " + StatisticState.findBCCount);
        System.out.println("        Tabu rejucted formule: " + StatisticState.tabuMemoryRejectCount);
        /**
         * LTL求解器 相关统计信息
         */
        System.out.println("【SOLVER INFO】");
        System.out.println("        LTL solver CALLs: " + callSolverCount +"    TIMEOUTs: "+ callSolverTimeoutCount + "    ERRORs: "+ callSolverErrorCount);
        System.out.println("        LTL solver time: " + String.format("%.2f", callSolverTime/1000.0) +
                String.format("(%.3f)", callSolverTime/1000.0/callSolverCount));

        System.out.println("【LOCAL GENERAL INFO】");
        if (InitialConfiguration.localGeneral) {
            System.out.println("        local general process time: " + String.format("%.2f", StatisticState.localGeneralTime/1000.0));
        }
        else{
            System.out.println("        Local general is disabled");
        }
        /**
         * 模型计数 相关统计信息
         */
        System.out.println("【MODEL COUNTING INFO】");
        if (InitialConfiguration.usingModelCounting) {
            System.out.println("        model counter start time: " + String.format("%.2f",StatisticState.modelCounterStartTime/1000.0));
            System.out.println("        call model counter time: " + String.format("%.2f",StatisticState.callModelCounterTime/1000.0));
            System.out.println("        call model counter count: " + StatisticState.callModelCounterCount);
            System.out.println("        zero for model counter count: " + StatisticState.modelCounterZeroCount);
        }
        else{
            System.out.println("        Model counting is disabled");
        }

        System.out.println("【RESTART INFO】");
        if(InitialConfiguration.usingRestart){
            System.out.println("        Restart count: " + StatisticState.restartCount);
        }
        else{
            System.out.println("        Restart strategy is disabled");
        }

        System.out.println("【WEAKENING BC PROCESS】");
        if (InitialConfiguration.weakenBC) {
            System.out.println("        weakened bc: " + weakenBCPair.size());
            System.out.println("        weakening BC process time: " + String.format("%.2f", StatisticState.weakenBCTime/1000.0));
        } else {
            System.out.println("        weakening BC process is disabled");
        }

        System.out.println("【SWLS (strengthen and weaken local search) INFO】");
        if (InitialConfiguration.swlsFlag) {
            System.out.println(String.format("        SWLS strategy count: %d, total time: %.2f, avg time: %.2f",
                    swlsStrategyCount, swlsStrategyTime/1000.0, swlsStrategyTime/1000.0/swlsStrategyCount));
            System.out.println(String.format("        SWLS strengthen count: %d, time: %.2f, avg time: %.2f",
                    StatisticState.swlsStrengthenCount, StatisticState.swlsStrengthenTime/1000.0,
                    StatisticState.swlsStrengthenTime/1000.0/swlsStrengthenCount));
            System.out.println(String.format("        SWLS weaken count: %d, time: %.2f, avg time: %.2f",
                    StatisticState.swlsWeakenCount, StatisticState.swlsWeakenTime/1000.0,
                    StatisticState.swlsWeakenTime/1000.0/swlsWeakenCount));
            System.out.println("        SWLS strengthen formula: " + swlsStrengthenFormulae.size() +
                    ", weaken formula: " + swlsWeakenFormulae.size());
            System.out.println("        SWLS trivial formula count: " + swlsTrivialFormula);
        } else {
            System.out.println("        SWLS is disabled");
        }

        System.out.println();

        /**
         * 打印完统计信息的后处理，比如统计信息输出到文件
         */

        if (InitialConfiguration.swlsDumpFile != null) {
            swlsDumpToFile(InitialConfiguration.swlsDumpFile);
        }
    }

    static protected void swlsDumpToFile(String filePath) {
        File file = new File(filePath);
        try {
            file.createNewFile();
            if (!file.canWrite()) { return; }

            /**
             * 预估 builder 的大小，不够可以自动增长
             */
            StringBuilder stringBuilder = new StringBuilder(50 + swlsWeakenFormulae.size()*100 + swlsStrengthenFormulae.size()*100);
            stringBuilder.append("#swlsStrengthenFormulae\n");
            for (Pair<Formula<String>, Formula<String>> pair : swlsStrengthenFormulae) {
                stringBuilder.append(pair.getKey().toPLTLString() + ", " + pair.getValue().toPLTLString() + "\n");
            }

            stringBuilder.append("##swlsWeakenFormulae\n");
            for (Pair<Formula<String>, Formula<String>> pair : swlsWeakenFormulae) {
                stringBuilder.append(pair.getKey().toPLTLString() + ", " + pair.getValue().toPLTLString() + "\n");
            }

            IOUtils.fileWrite(stringBuilder.toString(), filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

