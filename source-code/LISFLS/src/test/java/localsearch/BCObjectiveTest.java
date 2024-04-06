package localsearch;

import gov.nasa.ltl.trans.ParseErrorException;
import junit.framework.TestCase;
import ltlparse.Formula;
import ltlparse.Parser;
import main.InitialConfiguration;
import main.LogionState;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.junit.Assert;
import utils.ParserUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BCObjectiveTest extends TestCase {
    public void testEvaluate() throws ParseErrorException, IOException, InterruptedException {
        List<String> doms = Arrays.asList(
                "[]((p && X(p)) -> X(X(! h)))"
        );
        List<String> goals = Arrays.asList(
                "[](h -> X(p))",
                "[](m -> X(! p))"
        );

        List<Formula<String>> domsformula = new ArrayList<>();
        List<Formula<String>> goalsformula = new ArrayList<>();
        for (String str : doms) {
            domsformula.add(Parser.parse(str));
        }
        for (String str : goals) {
            goalsformula.add(Parser.parse(str));
        }

        LogionState.initalization(doms, goals);
        BCObjective objective = new BCObjective(domsformula, goalsformula);


        // String str = "!(G(h -> X p) & G(m -> X !p))";
        String str = "F(h & m)";
        Evaluation value = objective.evaluate(new BCSolution(ParserUtils.parserPLTL(str)), null);
        System.out.println(str + " evaluate: " + value.getValue());
        Assert.assertTrue(value.getValue() >= InitialConfiguration.objectiveValueMinScore);
    }
}