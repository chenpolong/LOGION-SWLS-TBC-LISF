package main;

import gov.nasa.ltl.trans.ParseErrorException;
import localsearch.BCData;
import localsearch.BCSolution;
import localsearch.InitializationType;
import ltlparse.Formula;
import ltlsolver.LTLSolverType;
import org.apache.commons.cli.*;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.search.LocalSearch;
import utils.Input;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    /**
     * 算法流程<br>
     * 1. {@link InitialConfiguration#parseCommandLine(CommandLine)} 将命令行指定参数覆盖默认参数 <br>
     * 2. {@link LogionState#initalization(List, List)} 输入 Doms, Gaols, 初始化 Logion 需要使用的类 <br>
     * 3. {@link BCLearner#learnBC(List, List)} 构造 local search 并启动搜索 <br>
     * 4. local search 过程 详细的内部过程见文档  下面说明自己实现类的调用顺序 <br>
     *      4.0 设置初时解 <br>
     *      4.1 {@link localsearch.BCNeighbourhood#getAllMoves(BCSolution)} 获取邻居集合 <br>
     *      4.2 {@link localsearch.BCObjective#evaluate(BCSolution, BCData)} 对每一个邻居进行评估 <br>
     *          4.2.1 {@link localgeneral.LocalGeneralDefaultImp#localGeneralProcess(Formula)} local general 过程 <br>
     *          4.2.2 {@link SolutionCollector} BC会被收集到此类 <br>
     *      4.3 {@link localsearch.ProgressSearchListener#newCurrentSolution(LocalSearch, BCSolution, Evaluation, Validation)} 每次迭代的回调函数 <br>
     *          4.3.1 {@link localsearch.LocalSearchRestart#isRestart(LocalSearch, BCSolution, Evaluation, Validation)} 判断是否满足重启条件 <br>
     * 5. 重复 步骤4 直到超时退出 <br>
     */
    public static void main(String[] args) throws ParseException, InterruptedException, IOException, ParseErrorException {
        List<String> doms = new ArrayList<>();
        List<String> goals = new ArrayList<>();

        Options options = new Options();
        // help
        options.addOption("h", "help", false, "help");
        // domain
        options.addOption("d", null, true,
                "Enter domains from the command line");
        // goal
        options.addOption("g", null, true,
                "Enter goals from the command line");
        // input file
        options.addOption("i", null, true,
                "Read domains and goals from file");
        // timeout
        options.addOption("t", null, true,
                "Maximum running time of algorithm (second >0, default=" + InitialConfiguration.localSearchTimeout + ")");
        // solver
        options.addOption("s", null, true,
                "Set up the LTL solver (" + LTLSolverType.allEnum() + ", default=" + InitialConfiguration.solverType + ")");
        // formula
        //options.addOption("f", null, true,
        //        "Evaluate this formula");
        // ltlcheckmaxtime
        options.addOption(null, "ltlcheckmaxtime", true,
                "Maximum solution time of LTL solver (second >0, default=" + InitialConfiguration.ltlCheckTimeout + ")");
        // algorithm score
        options.addOption(null, "debugScoreMode", false,
                "Used for automatic parameter adjustment");
        // 用于 自动调参平台的吸收 cutoff_time 报错
        options.addOption("cutoff_time", null, false,
                "For automatic parameter adjustment");
        // algorithm 初始化方式
        options.addOption(null, "initialization", true,
                "Initialization policy (" + InitializationType.allEnum() + ", default="+ InitialConfiguration.initializationPolicy + ")");
        options.addOption(null, "localgeneral", false,
                "Whether to turn on the local general option (, default="+ InitialConfiguration.localGeneral + ")");
        // 重启条件
        options.addOption(null, "restart", true,
                "The number of times the value of the objective function is continuously unchanged when the restart condition is reached (default=" +
                InitialConfiguration.restartCondition + ")");
        // 有界邻居选择值
        options.addOption("k", "neighbourhoodsKsize", true, "K Bounded neighbor selection)");
        // 模型计数边界
        options.addOption("MC", null, true,
                "use model counting(options: ABC/Cache/Likelyhood)");
        options.addOption(null, "modelCountingTimeout", true,
                "Timeout time for a single model count (default=" + InitialConfiguration.modelCountingTimeout + ")");
        options.addOption(null, "modelCountingBound", true,
                "The size of the model in the ABC model count (default=" + InitialConfiguration.modelCountingBound + ")");
        options.addOption(null, "refine", true,
                "if open the refine module");
        options.addOption(null, "core", false,
                "if open the witness core plugin");
        options.addOption(null, "weakeningBC", false,
                "use weakening bc process");
        options.addOption(null, "swls", false,
                "use swls (strengthen and weaken local search) algorithm, default: " +
                        (InitialConfiguration.swlsFlag ? "enable" : "disable"));
        options.addOption(null, "swlsWeaken", true,
                String.format("swls weaken probability (default: %.2f)", InitialConfiguration.swlsWeakenProbability));
        options.addOption(null, "swlsStrengthen", true,
                String.format("swls strengthen probability (default: %.2f)", InitialConfiguration.swlsStrengthenProbability));
        options.addOption(null, "swlsDumpFile", true,
                String.format("dump swls strengthen and weaken formulae to file (default: %s)", InitialConfiguration.swlsDumpFile));
        /**
         * 默认关闭权重设置
         */
        // addCommandLineWeight(options);

        CommandLineParser parser = new GnuParser(); //gun风格
        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption("h")) {
            //格式化输出
            new HelpFormatter().printHelp("LISFLS", options, true);
            return;
        }
        if (commandLine.hasOption("d")) {
            String[] strings = commandLine.getOptionValues("d");
            doms.addAll(Arrays.asList(strings));
        }
        if (commandLine.hasOption("g")) {
            String[] strings = commandLine.getOptionValues("g");
            goals.addAll(Arrays.asList(strings));
        }
        if (commandLine.hasOption("i")) {
            String string = commandLine.getOptionValue("i");
            Input input = new Input(new File(string));
            boolean ret = input.readDomainAndGoal(doms, goals);
            if (!ret) {
                System.err.println("output file=" + string + " error!");
                return;
            }
        }

        /**
         * 初始化  {@link InitialConfiguration} 和 {@link LogionState}
         * 先 {@link InitialConfiguration} 后 {@link LogionState}
         */
        InitialConfiguration.parseCommandLine(commandLine);
        LogionState.initalization(doms, goals);

        BCLearner learner = new BCLearner();
        try {
            learner.learnBC(doms, goals);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置终端输入公式编辑操作和LTL运算符权重
     * @param options
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    static void addCommandLineWeight(Options options) {
        String[] names = {"rename", "insertion", "deletion", "not", "next", "globally", "future",
                "and", "or", "iff", "until", "weakuntil"};
        for (String str : names) {
            options.addOption(null, str, true, str+ " weight(double)");
        }
    }
}
