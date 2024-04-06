package localsearch;

import junit.framework.TestCase;
import ltlparse.Formula;
import org.junit.Test;
import utils.ParserUtils;

public class BCSolutionTest extends TestCase {
    @Test
    public void testBCSolutionSwap() {
        Formula<String> formula1 = ParserUtils.parserPLTL("F(p&h)");
        Formula<String> formula2 = ParserUtils.parserPLTL("G(m)");
        BCSolution solution1 = new BCSolution(formula1);
        BCSolution solution2 = new BCSolution(formula2);

        System.out.println("Before swap: " + solution1);
        solution1.swap(solution2);
        System.out.println("After swap: " + solution1);
        solution1.swap(solution2);
        System.out.println("undo swap: " + solution1);
    }
}