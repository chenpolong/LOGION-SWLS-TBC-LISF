package ltlsolver;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utils.ParserUtils;

import java.util.Arrays;
import java.util.List;

public class TsetPltlSolver {
    PltlSolver pltlSolver = null;

    @Before
    public void initialition() throws ParseErrorException {
        List<Formula<String>> doms = Arrays.asList(
                Parser.parse("[]((p && X(p)) -> X(X(! h)))")
        );
        List<Formula<String>> goals = Arrays.asList(
                Parser.parse("[](h -> X(p))"),
                Parser.parse("[](m -> X(! p))")
        );

        pltlSolver = new PltlSolver(doms, goals);
    }

    @Test
    public void testCheckSAT_SAT() {
        Formula<String> formula = ParserUtils.parserPLTL("a & b");
        LTLCheckResult ret = pltlSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.SAT);
    }

    @Test
    public void testCheckSAT_UNSAT() {
        Formula<String> formula = ParserUtils.parserPLTL("a & !a");

        LTLCheckResult ret = pltlSolver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.UNSAT);
    }

    @Test
    public void testCheckInconsistency_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = pltlSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckInconsistency_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("X(h & p)");
        BCCheckResult ret = pltlSolver.checkInconsistency(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckMinimality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = pltlSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = pltlSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }

    @Test
    public void testCheckMinimality_NO() {
        Formula<String> formula = ParserUtils.parserPLTL("G(h & m)");
        BCCheckResult ret = pltlSolver.checkMinimality(formula, 0, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
        ret = pltlSolver.checkMinimality(formula, 1, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.NO);
    }

    @Test
    public void testCheckNonTriviality_YES() {
        Formula<String> formula = ParserUtils.parserPLTL("F(h & m)");
        BCCheckResult ret = pltlSolver.checkNonTriviality(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, BCCheckResult.YES);
    }
}
