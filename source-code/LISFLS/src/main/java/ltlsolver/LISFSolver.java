package ltlsolver;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import main.InitialConfiguration;
import main.LogionState;
import utils.ParserUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 一个 {@link LISFBaseSolver} 负责一个子属性，维护一个 lasso 集合
 * {@link LISFSolver} 由 1+G+1 个 {@link LISFBaseSolver} 组成，分别支持 BC 的三个属性
 *  - inconsistency  1个
 *  - minimality     G个
 *  - non-triviality 1个
 * 同时委托给 {@link NuXmvSolver#checkSAT(Formula, long)} 实现 {@link LTLSolver#checkSAT(Formula, long)} 接口
 */
public class LISFSolver implements LTLSolver {
    private LISFBaseSolver inconsistenyChecker;
    private List<LISFBaseSolver> minimalityChecker = new ArrayList<>();
    private LISFBaseSolver nontrivialityChecker;

    private LTLSolver ltlSolver;

    private List<Formula<String>> doms;
    private List<Formula<String>> goals;

    /**
     * 对 minimality 属性的判定顺序会更改, 优先判定经常起到过滤作用的 minimality 子属性
     * {@link #minimalityValue} 是 minimality 子属性的得分, 子属性过滤的公式越多,它的得分越高
     * @warning 只在输出统计信息的时候使用
     */
    private List<LISFBaseSolver.Pair> minimalityValue = new ArrayList<>();

    public LISFSolver(List<Formula<String>> doms, List<Formula<String>> goals) throws IOException, InterruptedException {
        if(InitialConfiguration.usingCore){
            this.doms = new ArrayList<>();
            this.goals = new ArrayList<>();
            for (Formula<String> formula : doms) {
                this.doms.add(formula);
            }
            for (Formula<String> formula : goals) {
                this.goals.add(formula);
            }
        }
        initialization(doms, goals);
    }

    void initialization(List<Formula<String>> doms, List<Formula<String>> goals) throws IOException, InterruptedException {
        String allDoms = "(TRUE)";
        for (Formula<String> formula : doms) {
            allDoms = allDoms + " & " + formula.toNuXmvLTL();
        }

        String allGoals = goals.get(0).toNuXmvLTL();
        for (int i = 1; i < goals.size(); i++) {
            allGoals = allGoals + " & " + goals.get(i).toNuXmvLTL();
        }

        List<String> ltls = new ArrayList<>();
        for (Formula<String> formula : doms) {
            ltls.add(formula.toPLTLString());
        }
        for (Formula<String> formula : goals) {
            ltls.add(formula.toPLTLString());
        }
        Set<String> vars = ParserUtils.getVariables(ltls);

        inconsistenyChecker = new LISFBaseSolver(allDoms + " & " + allGoals, vars, "inconsistency");
        nontrivialityChecker = new LISFBaseSolver("!("+allGoals + ")", vars, "nonTriviality");

        for (int removeIndex = 0; removeIndex < goals.size(); removeIndex++) {
            String model = allDoms;
            for (int i = 0; i < goals.size(); i++) {
                if (i != removeIndex) {
                    model += " & " + goals.get(i).toNuXmvLTL();
                }
            }
            minimalityChecker.add(new LISFBaseSolver(model, vars, "minimality_-"+removeIndex));
            minimalityValue.add(new LISFBaseSolver.Pair(removeIndex, 0.0));
        }

        ltlSolver = LogionState.ltlSolverFactory.getSolver(LTLSolverType.nuXmv);
    }

    /**
     * 对直接判定单个LTL公式是否满足, 不需要使用LISF, 会委托给 {@link NuXmvSolver#checkSAT(Formula, long)} 判定
     */
    @Override
    public LTLCheckResult _checkSAT(Formula<String> formula, long timeout) {
        return ltlSolver._checkSAT(formula, timeout);
    }

    @Override
    public BCCheckResult _checkInconsistency(Formula<String> candicateBC, long timeout) {
        LTLCheckResult checkSATret = inconsistenyChecker.joinSAT(candicateBC.toNuXmvLTL(), timeout);
        BCCheckResult ret = BCCheckResult.ERROR;
        switch (checkSATret) {
            case SAT:
                ret = BCCheckResult.NO;
                break;
            case UNSAT:
                ret = BCCheckResult.YES;
                break;
            case TIMEOUT:
                ret = BCCheckResult.TIMEOUT;
                break;
            case ERROR:
                ret = BCCheckResult.ERROR;
                break;
            case STOP:
                ret = BCCheckResult.STOP;
                break;
            default:
                ret = BCCheckResult.ERROR;
        }
        return ret;
    }

    @Override
    public BCCheckResult _checkMinimality(Formula<String> candicateBC, int index, long timeout) {
        LTLCheckResult checkSATret = getMinimalityCheckByIndex(index).joinSAT(candicateBC.toNuXmvLTL(), timeout);
        BCCheckResult ret = BCCheckResult.ERROR;
        switch (checkSATret) {
            case SAT:
                ret = BCCheckResult.YES;
                break;
            case UNSAT:
                ret = BCCheckResult.NO;
                break;
            case TIMEOUT:
                ret = BCCheckResult.TIMEOUT;
                break;
            case ERROR:
                ret = BCCheckResult.ERROR;
                break;
            case STOP:
                ret = BCCheckResult.STOP;
                break;
            default:
                ret = BCCheckResult.ERROR;
        }
        return ret;
    }

    @Override
    public BCCheckResult _checkNonTriviality(Formula<String> candicateBC, long timeout) {
        LTLCheckResult checkSATret = nontrivialityChecker.joinSAT("!(" + candicateBC.toNuXmvLTL() + ")", timeout);
        BCCheckResult ret = BCCheckResult.ERROR;
        switch (checkSATret) {
            case SAT:
                ret = BCCheckResult.YES;
                break;
            case UNSAT:
                ret = BCCheckResult.NO;
                break;
            case TIMEOUT:
                ret = BCCheckResult.TIMEOUT;
                break;
            case ERROR:
                ret = BCCheckResult.ERROR;
                break;
            case STOP:
                ret = BCCheckResult.STOP;
                break;
            default:
                ret = BCCheckResult.ERROR;
        }
        return ret;
    }

    private LISFBaseSolver getMinimalityCheckByIndex(int i) {
        return minimalityChecker.get(i);
    }

    public void dropout() {
        double totalTime = 0;
        double pathTime = 0;
        double modelCheckTime = 0;
        long totalCount = 0;
        long pathCount = 0;
        long modelCheckCount = 0;

        double inconsCheckPathTime = inconsistenyChecker.getModelCheckingaPathTime();
        double inconsModelCheckTime = inconsistenyChecker.getModelCheckingTime();
        System.out.println(String.format("Counter Example for inconsistency total time: %.2f, check a path: %.2f, model checking: %.2f", inconsCheckPathTime+inconsModelCheckTime,
                inconsCheckPathTime, inconsModelCheckTime));
        pathTime += inconsCheckPathTime;
        modelCheckTime += inconsModelCheckTime;
        pathCount += inconsistenyChecker.getModelCheckingaPathCount();
        modelCheckCount += inconsistenyChecker.getModelCheckingCount();
        System.out.println(String.format("Counter Example for inconsistency total count: %d, check a path: %d, model checking: %d",
                inconsistenyChecker.getModelCheckingaPathCount()+inconsistenyChecker.getModelCheckingCount(),
                inconsistenyChecker.getModelCheckingaPathCount(), inconsistenyChecker.getModelCheckingCount()));

        double miniCheckPathTime = 0;
        double miniModelCheckTime = 0;
        long miniCheckPathCount = 0;
        long miniModelCheckCount = 0;
        for (LISFBaseSolver checker : minimalityChecker) {
            miniCheckPathTime += checker.getModelCheckingaPathTime();
            miniModelCheckTime += checker.getModelCheckingTime();

            miniCheckPathCount += checker.getModelCheckingaPathCount();
            miniModelCheckCount += checker.getModelCheckingCount();
        }
        System.out.println(String.format("Counter Example for minimality total time: %.2f(%.3f), check a path: %.2f(%.3f), model checking: %.2f(%.3f)",
                miniCheckPathTime+miniModelCheckTime, (miniCheckPathTime+miniModelCheckTime)/(miniModelCheckCount+miniCheckPathCount),
                miniCheckPathTime, miniCheckPathTime/miniCheckPathCount,
                miniModelCheckTime, miniModelCheckTime/miniModelCheckCount));
        System.out.println(String.format("Counter Example for minimality total count: %d, check a path: %d, model checking: %d",
                miniCheckPathCount+miniModelCheckCount, miniCheckPathCount, miniModelCheckCount));
        pathTime += miniCheckPathTime;
        modelCheckTime += miniModelCheckTime;
        pathCount += miniCheckPathCount;
        modelCheckCount += miniModelCheckCount;


        double nontriCheckPathTime = nontrivialityChecker.getModelCheckingaPathTime();
        double nontriModelCheckTime = nontrivialityChecker.getModelCheckingTime();
        System.out.println(String.format("Counter Example for nontriviality total time: %.2f, check a path: %.2f, model checking: %.2f", nontriCheckPathTime+nontriModelCheckTime,
                nontriCheckPathTime, nontriModelCheckTime));
        System.out.println(String.format("Counter Example for nontriviality total count: %d, check a path: %d, model checking: %d",
                nontrivialityChecker.getModelCheckingaPathCount()+nontrivialityChecker.getModelCheckingCount(),
                nontrivialityChecker.getModelCheckingaPathCount(), nontrivialityChecker.getModelCheckingCount()));
        pathTime += nontriCheckPathTime;
        modelCheckTime += nontriModelCheckTime;
        pathCount += nontrivialityChecker.getModelCheckingaPathCount();
        modelCheckCount += nontrivialityChecker.getModelCheckingCount();

        totalTime = pathTime + modelCheckTime;
        totalCount = pathCount + modelCheckCount;

        System.out.println(String.format("Counter Example total time: %.2f(%.3f), check a path: %.2f(%.3f), model checking: %.2f(%.3f)",
                totalTime, totalTime/totalCount, pathTime,pathTime/pathCount, modelCheckTime,modelCheckTime/modelCheckCount));
        System.out.println(String.format("Counter Example total count: %d, check a path: %d, model checking: %d",
                totalCount, pathCount, modelCheckCount));


        inconsistenyChecker.printStatistic();
        for (LISFBaseSolver checker : minimalityChecker) {
            checker.printStatistic();
        }
        nontrivialityChecker.printStatistic();

        /**
         * 打印详细的信息
         */
        if (inconsistenyChecker.getTracesValue().size() > 0) {
            System.out.print("inconsistency: [");
            for (LISFBaseSolver.Pair pair : inconsistenyChecker.getTracesValue()) {
                System.out.print(pair.toString() + ", ");
            }
            System.out.println("]");
        }

        for (int i = 0; i < minimalityValue.size(); i++) {
            LISFBaseSolver checker = getMinimalityCheckByIndex(minimalityValue.get(i).index);
            if (minimalityValue.get(i).value > 0) {
                System.out.print("minimality_-" + (minimalityValue.get(i).index+1) + ": " + String.format("%.2f", minimalityValue.get(i).value) + " [");
                for (LISFBaseSolver.Pair pair : checker.getTracesValue()) {
                    System.out.print(pair.toString() + ", ");
                }
                System.out.println("]");
            }
        }

        if (nontrivialityChecker.getTracesValue().size() > 0) {
            System.out.print("nontriviality: [");
            for (LISFBaseSolver.Pair pair : nontrivialityChecker.getTracesValue()) {
                System.out.print(pair.toString() + ", ");
            }
            System.out.println("]");
        }
    }

    @Override
    public void addDomain(Formula<String> formula) throws IOException, InterruptedException {
        if(InitialConfiguration.usingCore){
            this.doms.add(formula);
            initialization(this.doms, this.goals);
        }
    }

}
