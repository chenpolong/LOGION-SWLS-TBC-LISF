package refine.goalimproving;

import refine.goalimproving.improvers.SimpleModify;
import refine.goalimproving.improvers.Strengthen;
import refine.goalimproving.improvers.Weaken;

public class GoalImproverFactory {

    static SimpleModify simpleModify = null;
    static Strengthen strengthen = null;
    static Weaken weaken = null;

    public void initialization(GoalImproverType type){
        if(type == GoalImproverType.SIMPLE){
            simpleModify = new SimpleModify();
        }
        else if(type == GoalImproverType.STRENGTHEN){
            strengthen = new Strengthen();
        }
        else if(type == GoalImproverType.WEAKEN){
            weaken = new Weaken();
        }
    }

    public GoalImprover getImprover(GoalImproverType type){
        if(type == GoalImproverType.SIMPLE){
            if(simpleModify == null){
                simpleModify = new SimpleModify();
            }
            return simpleModify;
        }
        else if(type == GoalImproverType.STRENGTHEN){
            if(strengthen == null){
                strengthen = new Strengthen();
            }
            return strengthen;
        }
        else if(type == GoalImproverType.WEAKEN){
            if(weaken == null){
                weaken = new Weaken();
            }
            return weaken;
        }
        return simpleModify;
    }

}
