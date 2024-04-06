package refine.goalimproving.improvers;

import ltlparse.Formula;
import refine.goalimproving.GoalImprover;

public class Weaken implements GoalImprover {
    @Override
    public Formula<String> improveGoal(Formula<String> oldGoal) {
        return oldGoal;
    }

}
