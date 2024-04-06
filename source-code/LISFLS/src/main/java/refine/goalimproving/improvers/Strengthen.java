package refine.goalimproving.improvers;

import ltlparse.Formula;
import refine.goalimproving.GoalImprover;

public class Strengthen implements GoalImprover {
    @Override
    public Formula<String> improveGoal(Formula<String> oldGoal) {
        return oldGoal;
    }

}
