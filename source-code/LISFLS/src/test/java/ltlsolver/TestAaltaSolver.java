package ltlsolver;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import main.LogionState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utils.ParserUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestAaltaSolver {
    AaltaSolver aaltaSolver = null;

    @Before
    public void initialition() throws ParseErrorException, IOException, InterruptedException {
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


        aaltaSolver = new AaltaSolver(domsformula, goalsformula);
    }

    @Test
    public void testCheckSAT_SAT() {
        Formula<String> formula = ParserUtils.parserPLTL("a & b");
        LTLCheckResult ret = aaltaSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.SAT);
    }

    @Test
    public void testCheckSAT_UNSAT() {
        Formula<String> formula = ParserUtils.parserPLTL("a & !a");

        LTLCheckResult ret = aaltaSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.UNSAT);
    }
    
    @Test
    public void testCheckInconsistency_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = aaltaSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckInconsistency_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("X(h & p)");
        BCCheckResult ret = aaltaSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckMinimality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = aaltaSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = aaltaSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckMinimality_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("G(h & m)");
        BCCheckResult ret = aaltaSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = aaltaSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckNonTriviality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = aaltaSolver.checkNonTriviality(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }
}
