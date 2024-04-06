package localsearch;


import main.InitialConfiguration;
import main.LogionState;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.Search;
import org.jamesframework.core.search.listeners.SearchListener;

public final class ProgressSearchListener implements SearchListener<BCSolution> {
    @Override
    public void searchStarted(Search<? extends BCSolution> search) {
        System.out.println(" >>> Search started");
    }

    @Override
    public void searchStopped(Search<? extends BCSolution> search) {
        double time = search.getRuntime()/1000.0;
        long step = search.getSteps();
        System.out.println(" >>> Search stopped (" + String.format("%.2f", time) + " sec, " + step + "  steps, " +
                "speed " + String.format("%.2f", step/time) + " steps/sec)");
    }

    @Override
    public void newBestSolution(Search<? extends BCSolution> search, BCSolution newBestSolution, Evaluation newBestSolutionEvaluation, Validation newBestSolutionValidation) {
        double time = search.getRuntime()/1000.0;
        System.out.println(" >>> ["+ String.format("%.2f", time) + " sec]" +
                " New best formula:  vaule=" + String.format("%.2f", newBestSolutionEvaluation.getValue()) + ", " + newBestSolution.getFormula().toPLTLString());
    }

    /**
     * local search 每论迭代都会调用该函数
     * @param search
     * @param newCurrentSolution
     * @param newCurrentSolutionEvaluation
     * @param newCurrentSolutionValidation
     */
    @Override
    public void newCurrentSolution(LocalSearch<? extends BCSolution> search, BCSolution newCurrentSolution, Evaluation newCurrentSolutionEvaluation, Validation newCurrentSolutionValidation) {
        double time = search.getRuntime()/1000.0;
        double value = newCurrentSolutionEvaluation.getValue();
        System.out.println( " >>> ["+ String.format("%.2f", time) + " sec]" +
                " New current formula:  vaule=" + String.format("%.2f", value) + ", " + newCurrentSolution.getFormula().toPLTLString());

        /**
         * 判定是否满足重启条件
         */
        if (InitialConfiguration.usingRestart && LogionState.localSearchRestart.isRestart(search, newCurrentSolution, newCurrentSolutionEvaluation, newCurrentSolutionValidation)) {
            LogionState.searchRestart = true;
        }
    }
}
