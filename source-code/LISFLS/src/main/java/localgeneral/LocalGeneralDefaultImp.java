package localgeneral;

import ltlparse.Formula;
import ltlsolver.LTLCheckResult;
import ltlsolver.LTLSolver;
import ltlsolver.LTLSolverType;
import main.InitialConfiguration;
import main.LogionState;
import main.StatisticState;

import java.util.*;

/**
 * 默认的实现 local general 的类，还没有抽象出接口
 */
public class LocalGeneralDefaultImp {
    public Set<Formula<String>> globalGeneralBCSet = new HashSet<>();
    public Set<Formula<String>> localGeneralBCSet = new HashSet<>();

    public double localGeneralProcess(Formula<String> bc) {
        long startTime = System.currentTimeMillis();
        double value = _localGeneralprocess(bc);
        StatisticState.localGeneralTime += System.currentTimeMillis() - startTime;
        return value;
    }

    public double _localGeneralprocess(Formula<String> bc) {
        /**
         * 优化后伪代码:
         * 1. 首先判定它是否是BC, 如果是bc, 执行下面流程
         * 2. (前置检查)检查该邻居 与 当前邻居BC集合 S1 的关系,
         *      a. 是否 存在 formula \in S1, 当前邻居 |= formula
         *          如果存在, 忽略当前邻居, 结束流程
         * 3. (前置检查)检查该邻居 与 S集合的关系
         *      a. 是否 存在 formula \in S, 当前邻居 |= formula
         *          如果存在在, 忽略当前邻居, 结束流程
         * 4. 检查该邻居 与 当前邻居BC集合 S1 的关系,
         *      b. 是否 存在 formula \in S1, formula |= 当前邻居
         *          如果存在, 删除S1 中这样的 formula, 同时删除它在 S
         *      c. 将 当前邻居 加入到 S1 中
         * 5. 检查该邻居 与 S集合的关系
         *      b. 是否 存在 formula \in S, formula |= 当前邻居
         *          如果存在, 删除该bc
         *      c. 将当前邻居加入到 S 中
         * 6. 下一个当前解中 S1 中考虑. 如果S1为空,则从全部邻居中考虑
         */
        LTLSolver localLTLSolver = LogionState.ltlSolverFactory.getSolver(LTLSolverType.aalta);

//        double value = 0;
        /**
         * 2. (前置检查)检查该邻居 与 当前邻居BC集合 S1 的关系,
         *      a. 是否 存在 formula \in S1, 当前邻居 |= formula,  ~formula & 当前邻居 UNSAT
         *          如果存在, 忽略当前邻居, 结束流程
         */
        for (Formula<String> formula : localGeneralBCSet) {
            LTLCheckResult ret = localLTLSolver.checkSAT(Formula.And(Formula.Not(formula), bc), InitialConfiguration.ltlCheckTimeout);
            if (ret == LTLCheckResult.UNSAT) {
                return bc.getGeneralDegree();
            }
        }
        /**
         * 3. (前置检查)检查该邻居 与 S集合的关系
         *      a. 是否 存在 formula \in S, 当前邻居 |= formula
         *          如果存在在, 忽略当前邻居, 结束流程
         */
        for (Formula<String> formula : globalGeneralBCSet) {
            LTLCheckResult ret = localLTLSolver.checkSAT(Formula.And(Formula.Not(formula), bc), InitialConfiguration.ltlCheckTimeout);
            if (ret == LTLCheckResult.UNSAT) {
                return bc.getGeneralDegree();
            }
        }
        /**
         * 4. 检查该邻居 与 当前邻居BC集合 S1 的关系,
         *      b. 是否 存在 formula \in S1, formula |= 当前邻居
         *          如果存在, 删除S1 中这样的 formula, 同时删除它在 S, 分数+1
         *      c. 将 当前邻居 加入到 S1 中
         */
        Iterator<Formula<String>> it = localGeneralBCSet.iterator();
        while (it.hasNext()) {
            Formula<String> formula = it.next();
            LTLCheckResult ret = localLTLSolver.checkSAT(Formula.And(Formula.Not(bc), formula), InitialConfiguration.ltlCheckTimeout);
            if (ret == LTLCheckResult.UNSAT) {
                bc.addGeneralDegree(formula.getGeneralDegree()+1);
                it.remove();
                globalGeneralBCSet.remove(formula);
            }
        }
        localGeneralBCSet.add(bc);
        /**
         * 5. 检查该邻居 与 S集合的关系
         *      b. 是否 存在 formula \in S, formula |= 当前邻居
         *          如果存在, 删除该 formula,  分数+1
         *      c. 将当前邻居加入到 S 中
         */
        it = globalGeneralBCSet.iterator();
        while (it.hasNext()) {
            Formula<String> formula = it.next();
            LTLCheckResult ret = localLTLSolver.checkSAT(Formula.And(Formula.Not(bc), formula), InitialConfiguration.ltlCheckTimeout);
            if (ret == LTLCheckResult.UNSAT) {
                bc.addGeneralDegree(formula.getGeneralDegree()+1);
                it.remove();
            }
        }
        globalGeneralBCSet.add(bc);
        return bc.getGeneralDegree();
    }

    public void preconditions() {
        localGeneralBCSet.clear();
    }

    public List<String> getGeneralBC() {
        List<String> ret = new ArrayList<>();
        for (Formula<String> bc : globalGeneralBCSet) {
            ret.add(bc.toPLTLString());
        }
        return ret;
    }

    public Set<Formula<String>> getLocalGeneralBCSet() {
        return localGeneralBCSet;
    }
}
