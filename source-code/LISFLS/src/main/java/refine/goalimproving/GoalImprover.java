package refine.goalimproving;

import ltlparse.Formula;

public interface GoalImprover {
    public Formula<String> improveGoal(Formula<String> oldGoal);
}
