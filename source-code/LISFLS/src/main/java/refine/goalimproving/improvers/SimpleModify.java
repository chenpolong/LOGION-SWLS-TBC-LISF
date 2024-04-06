package refine.goalimproving.improvers;

import localsearch.BCFormulaOperator;
import ltlparse.Formula;
import refine.goalimproving.GoalImprover;

import java.util.List;
import java.util.Random;

public class SimpleModify implements GoalImprover {
    Random random = new Random();
    public SimpleModify(){

    }
    @Override
    public Formula<String> improveGoal(Formula<String> oldGoal) {
        Formula<String> origin = oldGoal.clone();
        BCFormulaOperator bcFormulaOperator = new BCFormulaOperator(origin);
        List<BCFormulaOperator.FormulaNode> nodes = bcFormulaOperator.getNodesRef();
        int size = nodes.size();
        int level = random.nextInt(size);
        Formula<String> newFormula = new Formula<String>(Formula.Content.TRUE, null, null, "true");
        if (level == 0) {
            return newFormula;
        } else {
            BCFormulaOperator.FormulaNode fatherNode = nodes.get(nodes.get(level).fatherIndex);
            Formula<String> father = fatherNode.f;
            if (fatherNode.leftIndex == level) {
                father.addSub1(newFormula);
            } else {
                father.addSub2(newFormula);
            }
            return origin;
        }
    }
}
