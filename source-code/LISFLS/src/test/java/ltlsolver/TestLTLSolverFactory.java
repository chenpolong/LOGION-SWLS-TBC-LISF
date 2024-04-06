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

public class TestLTLSolverFactory {
    LTLSolverFactory factory;

    @Before
    public void initialition() throws ParseErrorException {
        List<Formula<String>> doms = Arrays.asList(
                Parser.parse("[]((p && X(p)) -> X(X(! h)))")
        );
        List<Formula<String>> goals = Arrays.asList(
                Parser.parse("[](h -> X(p))"),
                Parser.parse("[](m -> X(! p))")
        );

        factory = new LTLSolverFactory(doms, goals);

    }

    @Test
    public void testInitNuXmvSolver() {
        LTLSolver solver = factory.getSolver(LTLSolverType.nuXmv);
        Formula<String> formula = ParserUtils.parserPLTL("p & m");
        LTLCheckResult ret = solver.checkSAT(formula, 10L);
        System.out.println(formula.toPLTLString() + ": " + ret);
        Assert.assertEquals(ret, LTLCheckResult.SAT);
    }

}
