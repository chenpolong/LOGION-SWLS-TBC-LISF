package localsearch;

import junit.framework.TestCase;
import ltlparse.Formula;
import utils.ParserUtils;

public class BCMoveTest extends TestCase {

    public void testBCMove() {
        Formula<String> formula1 = ParserUtils.parserPLTL("F(p&h)");
        Formula<String> formula2 = ParserUtils.parserPLTL("G(m)");
        BCSolution solution1 = new BCSolution(formula1);

        BCMove move = new BCMove(formula2);

        System.out.println("Before move: " + solution1);
        move.apply(solution1);
        System.out.println("After move: " + solution1);
        move.undo(solution1);
        System.out.println("undo swap: " + solution1);
    }

}