package assessment;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import main.LogionState;
import org.apache.xpath.objects.XString;
import utils.ABC.ABCModelCounter;
import utils.ParserUtils;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GoalModifyAssessor {
    private List<String> goalsList = new LinkedList<>();
    private List<String> targetList = new LinkedList<>();
    private String alpha;
    private int bound = 10;

    public GoalModifyAssessor() throws ParseErrorException {
        for(Formula<String> dom : LogionState.formulaDoms){
            this.targetList.add(dom.toRLTL());
        }
        for(Formula<String> goal : LogionState.formulaGoals){
            this.goalsList.add(goal.toRLTL());
        }
        Set<String> allvar = ParserUtils.getVariables(LogionState.doms, LogionState.goals);
        StringBuilder alpha = new StringBuilder();
        alpha.append("[");
        boolean first = true;
        for (String str : allvar) {
            Formula<String> var = Parser.parse(str);
            if (first) {
                alpha.append(var.toRLTL().replace("(", "").replace(")", "").trim());
                first = false;
            } else {
                alpha.append("," + var.toRLTL().replace("(", "").replace(")", "").trim());
            }
        }
        alpha.append("]");
        this.alpha = alpha.toString();
    }

    public int findIndexOfTargetGoal(String bc) {
        BigInteger maxValue = BigInteger.ZERO;
        Formula<String> target = null;
        int result = -1;
        for(int i = 0; i < goalsList.size(); ++i){
            BigInteger currValue = assessGoalViaBC(goalsList.get(i), bc);
            if(currValue.compareTo(maxValue) == 1){
                maxValue = currValue;
                result = i;
            }
        }
        return result;
    }

    public BigInteger assessGoalViaBC(String goal, String bc) {
        this.targetList.add(goal);
        ABCModelCounter abcModelCounter = new ABCModelCounter(alpha, targetList, bound);
        BigInteger result = abcModelCounter.count(bc);
        targetList.remove(targetList.size()-1);
        return result;
    }
}
