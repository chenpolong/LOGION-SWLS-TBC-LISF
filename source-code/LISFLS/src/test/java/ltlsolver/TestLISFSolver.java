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

public class TestLISFSolver {
    LTLSolver lisfSolver = null;

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

        lisfSolver = new LISFSolver(domsformula, goalsformula);
    }

    @Test
    public void testCheckSAT_SAT() {
        Formula<String> formula = ParserUtils.parserPLTL("p & m");
        LTLCheckResult ret = lisfSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.SAT);
    }

    @Test
    public void testCheckSAT_UNSAT() {
        Formula<String> formula = ParserUtils.parserPLTL("p & !p");

        LTLCheckResult ret = lisfSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.UNSAT);
    }

    @Test
    public void testCheckInconsistency_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = lisfSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckInconsistency_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("X(h & p)");
        BCCheckResult ret = lisfSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckMinimality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = lisfSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = lisfSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        if (lisfSolver instanceof LISFSolver) {
            ((LISFSolver)lisfSolver).dropout();
        }
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckMinimality_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("G(h & m)");
        BCCheckResult ret = lisfSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = lisfSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckNonTriviality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = lisfSolver.checkNonTriviality(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }
}
