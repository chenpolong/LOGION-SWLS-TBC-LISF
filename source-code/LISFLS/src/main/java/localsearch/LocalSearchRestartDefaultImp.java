package localsearch;

import ltlparse.Formula;
import main.InitialConfiguration;
import main.LogionState;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.search.LocalSearch;
import utils.Polling;

public class LocalSearchRestartDefaultImp implements LocalSearchRestart {
    Polling<Formula<String>> candidates;

    double lastValue = Double.MIN_VALUE;
    int unchangedCount = 0;

    public LocalSearchRestartDefaultImp(Polling<Formula<String>> candidates) {
        this.candidates = candidates;
    }

    @Override
    public boolean _isRestart(LocalSearch<? extends BCSolution> search, BCSolution newCurrentSolution, Evaluation newCurrentSolutionEvaluation, Validation newCurrentSolutionValidation) {
        /**
         * 连续 {@link InitialConfiguration#restartCondition} 次迭代没有提升解的质量, 那么重启
         * 1. 如果当前解的得分和以前一样, {@link unchangedCount} +1
         * 2. 当 {@link unchangedCount} >= {@link InitialConfiguration#restartCondition} 时
         *      - {@link unchangedCount} = 0
         *      - 设置新的当前解
         */
        double value = newCurrentSolutionEvaluation.getValue();
        boolean ret = false;
        if (Math.abs(lastValue - value) < 10E-6) {
            unchangedCount++;
        } else {
            lastValue = value;
            unchangedCount = 0;
        }


        if (unchangedCount >= InitialConfiguration.restartCondition && !LogionState.searchStop) {
            ret = true;
            unchangedCount = 0;
        }
        return ret;
    }

    @Override
    public BCSolution _nextInitialSolution() {
        Formula<String> nextFormula = candidates.next();

        return new BCSolution(nextFormula.clone());
    }
}
