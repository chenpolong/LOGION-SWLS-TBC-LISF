package localsearch;

import main.StatisticState;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.search.LocalSearch;

/**
 * 重启策略的接口类，新的重启策略需要实现这个接口，然后注册到 {@link main.LogionState#localSearchRestart}
 */
public interface LocalSearchRestart {
    default boolean isRestart(LocalSearch<? extends BCSolution> search, BCSolution newCurrentSolution, Evaluation newCurrentSolutionEvaluation, Validation newCurrentSolutionValidation) {
        boolean ret = _isRestart(search, newCurrentSolution, newCurrentSolutionEvaluation, newCurrentSolutionValidation);
        if (ret) {
            StatisticState.restartCount++;
        }
        return ret;
    }
    boolean _isRestart(LocalSearch<? extends BCSolution> search, BCSolution newCurrentSolution, Evaluation newCurrentSolutionEvaluation, Validation newCurrentSolutionValidation);

    default BCSolution nextInitialSolution() {
        return _nextInitialSolution();
    }
    BCSolution _nextInitialSolution();
}
