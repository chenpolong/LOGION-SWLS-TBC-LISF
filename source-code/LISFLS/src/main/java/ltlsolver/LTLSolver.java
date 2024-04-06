package ltlsolver;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import main.StatisticState;

import java.io.IOException;
import java.util.List;

/**
 * LTLSolver 需要实现的接口
 * LTLSolver 调用顺序
 * 1. {@link LTLSolverFactory#getSolver(LTLSolverType)} 获取指定类型的 LTL求解器
 * 2. LTLSolver实现了 {@link LTLSolver} 接口，封装好了可满足性检查功能
 */
public interface LTLSolver {
    /**
     * 判定 公式 formula 的可满足性
     * @param formula
     * @param timeout 单位 秒
     * @return
     */
    LTLCheckResult _checkSAT(Formula<String> formula, long timeout);
    default LTLCheckResult checkSAT(Formula<String> formula, long timeout) {
        StatisticState.callSolverCount++;
        long time = System.currentTimeMillis();
        LTLCheckResult ret = _checkSAT(formula, timeout);
        long betweenTime = (System.currentTimeMillis()-time);
        StatisticState.ltlsatTime += betweenTime;
        StatisticState.callSolverTime += betweenTime;
        if (ret == LTLCheckResult.ERROR) {
            StatisticState.callSolverErrorCount++;
        } else if (ret == LTLCheckResult.TIMEOUT) {
            StatisticState.callSolverTimeoutCount++;
        }
        return ret;
    }

    /**
     * 分别检查 BC 的不相容是否满足， 输入是潜在的BC和超时时间
     * @param candicateBC
     * @param timeout   单位 秒
     * @return
     */
    BCCheckResult _checkInconsistency(Formula<String> candicateBC, long timeout);
    default BCCheckResult checkInconsistency(Formula<String> candicateBC, long timeout) {
        StatisticState.callSolverCount++;
        long time = System.currentTimeMillis();
        BCCheckResult ret = _checkInconsistency(candicateBC, timeout);
        long betweenTime = (System.currentTimeMillis()-time);
        StatisticState.logicalInconsistencyTime += betweenTime;
        StatisticState.callSolverTime += betweenTime;
        if (ret == BCCheckResult.ERROR) {
            StatisticState.callSolverErrorCount++;
        } else if (ret == BCCheckResult.TIMEOUT) {
            StatisticState.callSolverTimeoutCount++;
        }
        return ret;
    }

    /**
     * 提供细粒度的检查, 检查 dom & G_-i & candidateBC \not \model SAT
     * @param candicateBC
     * @param index:  是 G_-i 中的 i, 从0开始计数
     * @param timeout
     * @return
     */
    BCCheckResult _checkMinimality(Formula<String> candicateBC, int index ,long timeout);
    default BCCheckResult checkMinimality(Formula<String> candicateBC, int index, long timeout) {
        StatisticState.callSolverCount++;
        long time = System.currentTimeMillis();
        BCCheckResult ret = _checkMinimality(candicateBC, index, timeout);
        long betweenTime = (System.currentTimeMillis()-time);
        StatisticState.minimalityTime += betweenTime;
        StatisticState.callSolverTime += betweenTime;
        if (ret == BCCheckResult.ERROR) {
            StatisticState.callSolverErrorCount++;
        } else if (ret == BCCheckResult.TIMEOUT) {
            StatisticState.callSolverTimeoutCount++;
        }
        return ret;
    }

    /**
     * 只需要检查 trivialBC |= candicateBC
     * @param candicateBC
     * @param timeout
     * @return
     */
    BCCheckResult _checkNonTriviality(Formula<String> candicateBC, long timeout);
    default BCCheckResult checkNonTriviality(Formula<String> candicateBC, long timeout) {
        StatisticState.callSolverCount++;
        long time = System.currentTimeMillis();
        BCCheckResult ret = _checkNonTriviality(candicateBC, timeout);
        long betweenTime = (System.currentTimeMillis()-time);
        StatisticState.nonTrivialityTime += betweenTime;
        StatisticState.callSolverTime += betweenTime;
        if (ret == BCCheckResult.ERROR) {
            StatisticState.callSolverErrorCount++;
        } else if (ret == BCCheckResult.TIMEOUT) {
            StatisticState.callSolverTimeoutCount++;
        }
        return ret;
    }

    void addDomain(Formula<String> formula) throws IOException, InterruptedException;

}
