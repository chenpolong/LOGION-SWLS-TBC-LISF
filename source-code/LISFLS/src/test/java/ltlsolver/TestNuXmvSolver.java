package ltlsolver;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utils.ParserUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestNuXmvSolver {
    NuXmvSolver nuXmvSolver = null;

    @Before
    public void initialition() throws ParseErrorException, IOException, InterruptedException {
        List<Formula<String>> doms = Arrays.asList(
                Parser.parse("[]((p && X(p)) -> X(X(! h)))")
        );
        List<Formula<String>> goals = Arrays.asList(
                Parser.parse("[](h -> X(p))"),
                Parser.parse("[](m -> X(! p))")
        );
        List<String> vars = Arrays.asList(
                "p",
                "h",
                "m"
        );
        nuXmvSolver = new NuXmvSolver(vars, doms, goals);
    }

    @Test
    public void testCheckSAT_SAT() {
        Formula<String> formula = ParserUtils.parserPLTL("p & m");
        LTLCheckResult ret = nuXmvSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.SAT);
    }

    @Test
    public void testCheckSAT_UNSAT() {
        Formula<String> formula = ParserUtils.parserPLTL("p & !p");

        LTLCheckResult ret = nuXmvSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.UNSAT);
    }

    @Test
    public void testCheckInconsistency_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = nuXmvSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckInconsistency_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("X(h & p)");
        BCCheckResult ret = nuXmvSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckMinimality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = nuXmvSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = nuXmvSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckMinimality_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("G(h & m)");
        BCCheckResult ret = nuXmvSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = nuXmvSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckNonTriviality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = nuXmvSolver.checkNonTriviality(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }
}
