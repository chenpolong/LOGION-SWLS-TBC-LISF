package localsearch;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import ltlsolver.BCCheckResult;
import ltlsolver.LTLCheckResult;
import ltlsolver.LTLSolver;
import ltlsolver.LTLSolverFactory;
import main.BCInfo;
import main.InitialConfiguration;
import main.LogionState;
import main.StatisticState;
import org.jamesframework.core.problems.objectives.Objective;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.objectives.evaluations.SimpleEvaluation;
import utils.Pair;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BCObjective implements Objective<BCSolution, BCData> {
    private List<Formula<String>> domains;
    private List<Formula<String>> goals;

    private Map<String, Double> formulas2Value = new HashMap<>();

    private double LOGICAL_INCONSISTENCY = InitialConfiguration.objectiveValueInconsistency;
    private double MINIMALITY            = InitialConfiguration.objectiveValueMinimality;
    private double NONTRIVIALLITY        = InitialConfiguration.objectiveValueNontriviality;
    private double MIN_SOLUTION_VALUE    = InitialConfiguration.objectiveValueMinScore;

    private LTLSolver ltlSATCheck;

    public BCObjective(List<Formula<String>> domains, List<Formula<String>> goals) {
        this.domains = domains;
        this.goals = goals;
        ltlSATCheck = new LTLSolverFactory(domains, goals).getSolver(InitialConfiguration.solverType);
    }

    /**
     * 1. 首先查找 {@link #formulas2Value} 是否有solution的评分,有则直接返回
     * 2. 没有则调用 {@link #evaluateInvokeDefault(BCSolution solution)} 计算评分,并保存到 {@link #formulas2Value}
     * @param solution
     * @param bcData 用不到，填 null 即可
     * @return
     */
    @Override
    public Evaluation evaluate(BCSolution solution, BCData bcData) {
        if (LogionState.searchStop) { return SimpleEvaluation.WITH_VALUE(0); }

        Formula<String> formula = solution.getFormula();
        StatisticState.callEvaluateCount++;
        if (formulas2Value.containsKey(formula.toString())) {
            double value = formulas2Value.get(formula.toString());
            if (value >= MIN_SOLUTION_VALUE) { StatisticState.findBCCount++; }
            return SimpleEvaluation.WITH_VALUE(value);
        }
        StatisticState.callEvaluateDistinctCount++;

        long time = System.currentTimeMillis();
        SimpleEvaluation value = null;
        try {
            value = evaluateInvokeDefault(solution);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseErrorException e) {
            e.printStackTrace();
        }
        StatisticState.callEvaluateTime += (System.currentTimeMillis()-time);

        formulas2Value.put(formula.toString(), value.getValue());

        if (value.getValue() >= MIN_SOLUTION_VALUE) {
            StatisticState.findBCCount++;
            StatisticState.findBCDistinceCount++;
        }

        return value;
    }

    public double getValueByFormula(Formula<String> formula) {
        if (LogionState.searchStop) { return 0; }
        if (formulas2Value.containsKey(formula.toString())) {
            return formulas2Value.get(formula.toString());
        }
        return 0;
    }


    @Override
    public boolean isMinimizing() {
        return false;
    }

    /**
     * 输入 formula, 输出 它的分数
     * @param solution
     * @return
     */
    private SimpleEvaluation evaluateInvokeDefault(BCSolution solution) throws IOException, InterruptedException, ParseErrorException {
        Formula<String> formula = solution.getFormula();
        /**
         * check first if it is equivalent to True or False
         */
        if (ltlSATCheck.checkSAT(formula, InitialConfiguration.ltlCheckTimeout) == LTLCheckResult.UNSAT) {
            StatisticState.findContradictory++;
            return SimpleEvaluation.WITH_VALUE(0);
        } else if (ltlSATCheck.checkSAT(Formula.Not(formula), InitialConfiguration.ltlCheckTimeout) == LTLCheckResult.UNSAT) {
            StatisticState.findTautology++;
            return SimpleEvaluation.WITH_VALUE(0);
        }

        Boolean isSolution                  = false;
        double fitnessValue                 = 0;
        double logical_inconsistency_value  = 0;
        double minimality_value             = 0;
        double non_triviality_value         = 0;
        BigDecimal modelpercent             = new BigDecimal(0);
        double regularValue                 = 0;
        boolean shortCircuit                = false;
        double generalValue                 = 0;

        /**
         * 检查 minimality
         */
        if (!shortCircuit) {
            int minimality_count = 0;
            for (int i = 0; i < goals.size(); i++) {
                BCCheckResult ret = ltlSATCheck.checkMinimality(formula, i, InitialConfiguration.ltlCheckTimeout);
                if (ret == BCCheckResult.YES) {
                    minimality_count++;
                }
            }
            if (minimality_count == goals.size()) {
                minimality_value = MINIMALITY;
            } else {
                minimality_value = (MINIMALITY * minimality_count) / goals.size();
                shortCircuit = true;
            }
            fitnessValue += minimality_value;
        }

        /**
         * 检查 logical inconsistency
         */
        if (!shortCircuit) {
            BCCheckResult ret = ltlSATCheck.checkInconsistency(formula, InitialConfiguration.ltlCheckTimeout);
            if (ret == BCCheckResult.YES) {
                logical_inconsistency_value = LOGICAL_INCONSISTENCY;
            } else {
                shortCircuit = true;
            }
//            System.out.println(ret);
            fitnessValue += logical_inconsistency_value;
        }

        /**
         * 检查 non-triviality
         */
        if (!shortCircuit) {
            BCCheckResult ret = ltlSATCheck.checkNonTriviality(formula, InitialConfiguration.ltlCheckTimeout);
            if (ret == BCCheckResult.YES) {
                non_triviality_value = NONTRIVIALLITY;
            } else {
                shortCircuit = true;
            }
            fitnessValue += non_triviality_value;
        }

        if (fitnessValue >= MIN_SOLUTION_VALUE) {
            isSolution = true;
        }

        /**
         * 二阶段目标函数
         */
        if (isSolution) {
            regularValue = 1.0 / formula.size();
            fitnessValue += regularValue;
//            if (InitialConfiguration.usingModelCounting && LogionState.canModelCounting) {
//                // TODO 模型计数
//                BigInteger ret = LogionState.modelCounter.count(formula.toRLTL());
//                int scale =  LogionState.modelCounter.domainModelScale();
//                modelpercent = HighPrecisionUtils.bigIntegerDivide(ret, LogionState.modelCounter.getDomModelCount(), scale);
//                fitnessValue += modelpercent.doubleValue();
//            }
            if (InitialConfiguration.localGeneral) {
                generalValue = LogionState.localGeneralImp.localGeneralProcess(formula);
                fitnessValue += generalValue;
            }

        }

        /**
         * 将BC信息写入到结果集合中
         */
        if (isSolution) {
            if(InitialConfiguration.usingCore){
                Formula<String> bc = solution.getFormula();
                Formula<String> domain = bc.negate();
                addDomain(domain);
            }
            BCInfo bcInfo = new BCInfo(formula.toPLTLString());
            bcInfo.setTime((System.currentTimeMillis()-LogionState.startTime)/1000.0);
            bcInfo.setModelCounting(modelpercent);
            bcInfo.setRegularValue(regularValue);
            bcInfo.setValue(fitnessValue);
            bcInfo.setLocalGeneralValue(generalValue);
            LogionState.solutionCollector.addBCInfo(bcInfo);
            if(InitialConfiguration.usingRefine){
                LogionState.refinement.refine(formula);
            }

            if (InitialConfiguration.weakenBC && !isWeakenBCProcess) {
                // 对BC进行弱化，并且判断弱化后还是否是BC
                isWeakenBCProcess = true;
                long weakenStartTime = System.currentTimeMillis();
                /**
                 * weakenFormulaForLevelTraversal 排列按照层次遍历
                 */
                List<Formula<String>> weakenFormulae = solution.getFormula().weakenFormulaForLevelTraversal();
                for (Formula<String> weakenFormula : weakenFormulae) {
                    BCSolution weakenSolution = new BCSolution(weakenFormula);
                    Evaluation ret = this.evaluate(weakenSolution, LogionState.bcData);
                    if (ret.getValue() >= MIN_SOLUTION_VALUE) {
                        StatisticState.weakenBCPair.add(new Pair<>(solution, weakenSolution));
                        break;
                    }
                }
                isWeakenBCProcess = false;
                StatisticState.weakenBCTime += (System.currentTimeMillis() - weakenStartTime);
            }
        }

        return SimpleEvaluation.WITH_VALUE(fitnessValue);
    }

    /**
     * 当前是否是 weakenBC process 的过程
     * 只允许在非 weakenBC 过程中调用 weakenBC函数
     * 避免出现无限递归，或者递归深度过大，导致迭代很慢
     */
    protected boolean isWeakenBCProcess = false;

    void addDomain(Formula<String> formula) throws IOException, InterruptedException, ParseErrorException {
        ltlSATCheck.addDomain(formula);
        this.domains.add(formula);
        /**
         * TODO 检查算法是否停止, 检查  D & gi & ! G-i & B
         */
        String stopCondition = " (true)";
        for(Formula<String> domain : domains){
            stopCondition += ( " & " + domain.toNuXmvLTL());
        }
        stopCondition += (" & " + goals.get(0).toNuXmvLTL());
        if(goals.size() == 2){
            stopCondition += (" & ! (" + goals.get(1).toNuXmvLTL() + ")");
        }
        else{
            stopCondition += (" & ! (" + goals.get(1).toNuXmvLTL());
            for(int i = 2; i < goals.size(); ++i){
                stopCondition += ( " & " + goals.get(i).toNuXmvLTL());
            }
            stopCondition += ")";
        }
        stopCondition = stopCondition.replace(" & ", " && ").replace(" | ", " || ").replace(" G ", " [] ").replace(" F ", " <> ").replace(" G(", " [](").replace(" F(", " <>(");
        System.out.println("bcobject "+ stopCondition);
        LTLCheckResult ret = ltlSATCheck.checkSAT(Parser.parse(stopCondition), InitialConfiguration.ltlCheckTimeout);
        System.out.println(ret);
        if(ret == LTLCheckResult.UNSAT){
            LogionState.stopByTime = true;
            LogionState.searchStop = true;
        }
//        Formula<String> stopCondition = new Formula<>(Formula.Content.TRUE, null, null, null);
//        BCCheckResult res = ltlSATCheck.checkMinimality(stopCondition, 0, InitialConfiguration.ltlCheckTimeout);
//        if(res == BCCheckResult.NO){
//            LogionState.searchStop = true;
//        }
    }
}