package ltlsolver;

import main.InitialConfiguration;
import main.LogionState;
import modelcounting.Cache.ModelCacheCounter;
import modelcounting.ModelCounterType;
import utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 一个 {@link LISFBaseSolver} 负责一个子属性，维护一个 lasso 集合
 */
public class LISFBaseSolver {
    /**
     * 可调参数
     */
    final static public int MAXTRACES           = InitialConfiguration.LISFmaxLasso;
    final static public double depreciationRate = InitialConfiguration.LISFdepreciationRate;
    static public long PATHMAXTIME              = InitialConfiguration.LISFModelCheckAPathTimeout;
    final static public double initialValue     = InitialConfiguration.LISFlassoinitialValue;

    /**
     * 文件路径
     */
    static private final String modelPath       = InitialConfiguration.modelDir + File.separator;
    static private final String traceParentPath = InitialConfiguration.tempDir + File.separator;
    static private final String ltl2smv         = InitialConfiguration.ltl2smv + " 1 ";
    static private final String nuXmvCmd        = InitialConfiguration.nuXmv + " -int";


    /**
     * 统计数据
     */
    private int minHitIndex                       = MAXTRACES+1;
    private double avgHitIndex                    = 0;
    private long hitCallCount                     = 0;
    private long missCallCount                    = 0;
    private long cannotHitcount                   = 0;
    private long totalCallCount                   = 0;
    private int modelCheckingaPathTimeoutCount    = 0;
    private long modelCheckingaPathTime           = 0;
    private long modelCheckingaPathMaxTime        = 0;
    private long modelCheckingaPathCount          = 0;

    private long modelCheckingCount               = 0;
    private int modelCheckingTimeoutCount         = 0;
    private long modelCheckingTime                = 0;
    private long modelCheckingMaxTime             = 0;

    public double getModelCheckingaPathTime() {
        return modelCheckingaPathTime/1000.0;
    }

    public double getModelCheckingTime() {
        return modelCheckingTime/1000.0;
    }

    public long getModelCheckingCount() {
        return modelCheckingCount;
    }

    public long getModelCheckingaPathCount() {
        return modelCheckingaPathCount;
    }

    public void printStatistic() {
        if (totalCallCount == 0) {
            return ;
        }
        String out = "CounterExampleBaseLTLChecker(" + description + "):\n" +
                String.format("\t[Call: %d, Hit: %d(%.2f), Miss: %d (CannotHit: %d)]\n", totalCallCount, hitCallCount, hitCallCount/(double)totalCallCount, missCallCount, cannotHitcount) +
                String.format("\t[HitIndex: min: %d, avg: %.3f]\n", minHitIndex, avgHitIndex) +
                String.format("\t[modelCheckingaPath: count: %d, timeoutCount: %d, time: %.2f(avg: %.3f), maxTime: %.2f]\n",
                        modelCheckingaPathCount, modelCheckingaPathTimeoutCount, modelCheckingaPathTime/1000.0, modelCheckingaPathTime/1000.0/modelCheckingaPathCount,
                        modelCheckingaPathMaxTime/1000.0) +
                String.format("\t[modelChecking:      count: %d, timeoutCount: %d, time: %.2f(avg: %.3f), maxTime: %.2f]",
                        modelCheckingCount, modelCheckingTimeoutCount, modelCheckingTime/1000.0, modelCheckingTime/1000.0/modelCheckingCount,
                        modelCheckingMaxTime/1000.0);

        System.out.println(out);
    }

    /**
     * ltls 的合取
     */
    private String allLTLs;

    /**
     * 字母表
     */
    private Set<String> variables;

    private String description;

    private String tracePath;

    /**
     * model 路径
     */
    private String model;
    private String universalModel;

    int tracesCount = 0;
    List<Pair> tracesValue = new ArrayList<>();

    public LISFBaseSolver(String M, Set<String> variables, String description) throws IOException, InterruptedException {
        /**
         * 预处理
         */
        this.description = description;
        this.model = modelPath + description + ".model";
        this.universalModel = modelPath + "universal_" + description + ".model";
        this.tracePath = traceParentPath + description + "/";;
        this.allLTLs = M;
        this.variables = variables;
        initialize();
    }

    protected void initialize() throws IOException, InterruptedException {
        /**
         * 创建 tracePath 文件夹
         */
        File dir = new File(tracePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        /**
         * 创建 modelPath 文件夹
         */
        dir = new File(modelPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        /**
         * 生成 model
         */
        String tempFile = modelPath+"temp.txt";
        IOUtils.fileWrite(allLTLs, tempFile);
        Process p = IOUtils.invoke(ltl2smv + tempFile + " " + model+".unregulate", null); // 调用 ltl2smv
        p.waitFor();
        regulate(model+".unregulate", model);

        /**
         * 生成 universal.model
         */
        String line = "";
        StringBuffer buf = new StringBuffer(); //保存读入的内容
        BufferedReader bufferedReader = new BufferedReader(new FileReader(model));
        while( (line = bufferedReader.readLine()) != null ){
            if (line.startsWith("INIT")) { break;}
            buf.append(line+"\n");
        }  //修改文件
        bufferedReader.close();
        String content = buf.toString();
        IOUtils.fileWrite(content, universalModel);
    }

    /**
     * global \sum = [}
     * global const M
     * Algorithm counterExampleBaseLTLChecker:
     * Input: \phi
     * Output: SAT if M & \phi is SAT, UNSAT otherwise.
     *  \sum.sort()
     *  forall trace \in \sum do
     *      st <- ModelCheckaPath(trace.path, \phi)
     *      if st = True then
     *          // means M & \phi is SAT. trace is a model of M & \phi
     *          value(trace) <- value(trace) + 1
     *          return SAT
     *      else
     *          value(trace) <- value(trace) * depreciationRate
     *  st, trace <- ModelCheck(M, \neg \phi)
     *  if st = True then
     *      // M |= \neg formula. i,e,. M & formula is UNSAT
     *      return UNSAT
     *  else
     *      // M \not |= \neg \phi. i,e,. M & \phi is SAT
     *      // then counterexample trace holds trace |= M and t |= \phi
     *      \sum <- \sum U {trace}
     *  return SAT
     *
     * @param formula: nuXmv formula
     * @param timeout: 秒
     * @return 返回模型 M & formula 是否 SAT, 即 是否有交集
     */
    public LTLCheckResult joinSAT(String formula, long timeout) {
        if (LogionState.searchStop) { return LTLCheckResult.STOP; }
        totalCallCount++;
        LTLCheckResult sat = LTLCheckResult.ERROR;
        long startTime = System.currentTimeMillis();
        int count = 0;
        for (Pair pair : tracesValue) {
            count ++;
            int traceIndex = pair.index;

            LTLCheckResult ret = modelCheckingaPath(generateAddress(traceIndex), formula);
            if (ret == LTLCheckResult.STOP) { sat = ret; break; }

            if (ret == LTLCheckResult.SAT) {
                // Dom & G & \varphi is SAT, \varphi is not a bc
                /**
                 * trace 命中, +1
                 */
                sat = ret;
                hitCallCount++;
                pair.value += 1;

                break;
            } else {
                /**
                 * trace 未命中, 乘上折旧率
                 */
                pair.value *= depreciationRate;
            }
        }
        if (count != 0) {
            if (minHitIndex > count) { minHitIndex = count; }
            avgHitIndex = (avgHitIndex * (totalCallCount-1) + count) / totalCallCount;
        }
        long duringTime = System.currentTimeMillis() - startTime;
        modelCheckingaPathTime += duringTime;
        if (duringTime > modelCheckingaPathMaxTime) {
            modelCheckingaPathMaxTime = duringTime;
        }

        /**
         * 所有 trace 都未命中, 生成对应反例,更新并排序 traces 列表
         */
        if (sat == LTLCheckResult.ERROR) {
            missCallCount++;
            startTime = System.currentTimeMillis();
            sat = generateTraces("!("+formula+")", timeout);
            duringTime = System.currentTimeMillis() - startTime;
            modelCheckingTime += duringTime;
            if (duringTime > modelCheckingMaxTime) {
                modelCheckingMaxTime = duringTime;
            }
        }

        if (sat == LTLCheckResult.UNSAT) {
            cannotHitcount++;
        }

        updateTraces(tracesValue);


        return sat;
    }

    protected LTLCheckResult modelCheckingaPath(String traceFilePath, String formula) {
        if (LogionState.searchStop) { return LTLCheckResult.STOP; }
        modelCheckingaPathCount++;

        final String mcPathCmd = "read_model -i '" +universalModel+  "'\n" +
                "flatten_hierarchy\n" +
                "encode_variables\n" +
                "build_boolean_model\n" +
                "read_trace " +traceFilePath+ "\n" +
                "bmc_setup\n" +
                "check_ltlspec_on_trace -p '" +formula+ "' 1\n" +
                "quit\n";

        LTLCheckResult ret = LTLCheckResult.ERROR;
        try {
            Process p = IOUtils.invoke(nuXmvCmd, mcPathCmd);

            boolean timeoutFlag = false;
            if ( !p.waitFor(PATHMAXTIME, TimeUnit.SECONDS) ) {
                timeoutFlag = true;
                p.destroy();
            }

            if (timeoutFlag) {
                modelCheckingaPathTimeoutCount++;
                ret = LTLCheckResult.TIMEOUT;
                p.destroy();
            } else {
                String aux;
                String line = "";
                InputStream in = p.getInputStream();
                InputStreamReader inread = new InputStreamReader(in);
                BufferedReader bufferedreader = new BufferedReader(inread);
                while ((aux = bufferedreader.readLine()) != null) {
                    if (aux.startsWith("The property")) {
                        line = aux;
                        if (line.contains("is satisfied")) {
                            ret = LTLCheckResult.SAT;
                        } else if (line.contains("is not satisfied")) {
                            ret = LTLCheckResult.UNSAT;
                        } else {
                            ret = LTLCheckResult.ERROR;
                        }
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    protected LTLCheckResult generateTraces(String negformula, long timeout) {
        modelCheckingCount++;

        final String modelcheckCmd = "read_model -i '" + model + "'\n" +
                "flatten_hierarchy\n" +
                "encode_variables\n" +
                "build_boolean_model\n" +
                "check_ltlspec_ic3 -p '" +negformula+ "'\n" +
                "show_traces -p 4 -o '" +generateAddress(tracesCount+1)+ "'\n" +
                "quit\n";

        LTLCheckResult ret = LTLCheckResult.ERROR;
        try {
            Process p = IOUtils.invoke(nuXmvCmd, modelcheckCmd);

            boolean timeoutFlag = false;
            if ( !p.waitFor(timeout, TimeUnit.SECONDS)) {
                timeoutFlag = true;
                p.destroy();
            }

            if (timeoutFlag) {
                modelCheckingTimeoutCount++;
                ret = LTLCheckResult.TIMEOUT;
                p.destroy();
            } else {
                String aux;
                String line = "";
                InputStream in = p.getInputStream();
                InputStreamReader inread = new InputStreamReader(in);
                BufferedReader bufferedreader = new BufferedReader(inread);
                while ((aux = bufferedreader.readLine()) != null) {
                    if (aux.startsWith("-- LTL specification")) {
                        line = aux;
                        if (line.contains("is false")) {
                            // Dom & G & formula is SAT
                            tracesCount++;
                            tracesValue.add(new Pair(tracesCount, initialValue));
                            ret = LTLCheckResult.SAT;
                        } else if (line.contains("is true")) {
                            // Dom & G & formula is UNSAT
                            ret = LTLCheckResult.UNSAT;
                        } else {
                            ret = LTLCheckResult.ERROR;
                        }
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * 根据下标找到对应的文件
     * @param index
     * @return
     */
    protected String generateAddress(int index) {
        return tracePath + "traces_" + index + ".xml";
    }

    /**
     * 对 traces 重新排序
     */
    protected void updateTraces(List<Pair> traces) {
        Collections.sort(traces);

        if (traces.size() > 0 && traces.get(traces.size()-1).value < (initialValue/2)) {
            traces.remove(traces.size()-1);
        }

        if (traces.size() > MAXTRACES) {
            traces.remove(traces.size()-1);
        }
    }

    public String getDescription() {
        return description;
    }

    static protected class Pair implements Comparable<Pair> {
        public int index;
        public double value;
        public Pair(int index, double value) {
            this.index = index;
            this.value = value;
        }

        public Pair(int index) {
            this.index = index;
            this.value = 1;
        }

        @Override
        public int compareTo(Pair pair) {
            double ret = pair.value - this.value;
            if (ret > 0) {
                return 1;
            } else if (ret < 0) {
                return -1;
            } else {
                return (pair.index - this.index);
            }
        }

        @Override
        public String toString() {
            return "(" +
                    "" + index +
                    ", " + String.format("%.3f", value) +
                    ')';
        }
    }

    public List<Pair> getTracesValue() {
        return tracesValue;
    }

    protected void regulate(String file, String outputfile) throws IOException {
        BufferedReader bufferedReader = null;
        String line = null;
        StringBuffer buf = new StringBuffer(); //保存读入的内容
        bufferedReader = new BufferedReader(new FileReader(file));
        while( (line = bufferedReader.readLine()) != null ){
            if(line.startsWith("MODULE"))
                buf.append("MODULE main\n");
            else if (line.startsWith("VAR")){
                String var = "VAR\n";
                for(String s: variables){
                    var += "   "+ s + ": boolean;\n";
                }
                buf.append(var);
            }
            else
                buf.append(line + "\n");
        }  //修改文件
        bufferedReader.close();

        String content = buf.toString();
        IOUtils.fileWrite(content, outputfile);
    }
}
