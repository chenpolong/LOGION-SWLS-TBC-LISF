package localsearch;

import main.InitialConfiguration;
import main.StatisticState;
import org.jamesframework.core.search.algo.tabu.TabuMemory;
import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.util.FastLimitedQueue;

public final class BCTabuMemory implements TabuMemory<BCSolution> {
    /**
     * tabuTable 用于匹配是否在表中
     * tabuList  用于控制只禁忌前tabuMemorySize名的公式
     */
    FastLimitedQueue<String> limitedQueue = new FastLimitedQueue<>(InitialConfiguration.tabuMemorySize);

    static String lastpltlFormula = null;
    static String lastFormula     = null;

    @Override
    public boolean isTabu(Move<? super BCSolution> move, BCSolution currentSolution) {
//        return false;
        move.apply(currentSolution);
        boolean tabu = limitedQueue.contains(currentSolution.getFormula().toString());
        move.undo(currentSolution);

        if (tabu) { StatisticState.tabuMemoryRejectCount++; }
        return tabu;
    }

    @Override
    public void registerVisitedSolution(BCSolution visitedSolution, Move<? super BCSolution> appliedMove) {
        limitedQueue.add(visitedSolution.getFormula().toString());
        lastFormula     = visitedSolution.getFormula().toString();
        lastpltlFormula = visitedSolution.getFormula().toPLTLString();
    }

    @Override
    public void clear() {
        limitedQueue.clear();
    }
}
