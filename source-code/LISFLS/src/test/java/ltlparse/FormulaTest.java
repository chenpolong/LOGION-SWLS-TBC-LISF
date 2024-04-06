package ltlparse;

import org.junit.Test;
import utils.ParserUtils;

import java.util.List;
import java.util.Set;

/**
 * @author Deng
 * @date 2020/07/16
 * @time 20:26
 * @name ltlparse.FormulaTest
 */
public class FormulaTest {
    @Test
    public void copyAndReplaceTest() {
        Formula<String> formula1 = ParserUtils.parserPLTL("G(a -> b)");
        System.out.println(formula1.toPLTLString());
        Formula left = formula1.getSub1();
        Formula<String> formula2 = formula1.copyAndReplace(formula1, left, Formula.True());
        System.out.println("formula1: " + formula1.toPLTLString());
        System.out.println("formula2: " + formula2.toPLTLString());
    }

    @Test
    public void computePolarityTest() {
        Formula<String> formula1 = ParserUtils.parserPLTL("F(!(a & !G(b)) | c)");
        formula1.computePolarity();
        System.out.println(formula1.getPolarity() + ", " + formula1.toPLTLString());
        Set<Formula<String>> subFormulae = formula1.getSubFormulas();
        for (Formula<String> formula : subFormulae) {
            System.out.println(formula.getPolarity() + ", " + formula.toPLTLString());
        }
    }

    @Test
    public void weakenFormulaTest() {
        Formula<String> formula1 = ParserUtils.parserPLTL("F(!(a & !G(b)) | c)");
        System.out.println(formula1.toPLTLString());
        formula1.computePolarity();
        Set<Formula<String>> subFormulae = formula1.getSubFormulas();
        for (Formula<String> formula : subFormulae) {
            System.out.println(formula.getPolarity() + ", " + formula.toPLTLString());
        }
        System.out.println();
        List<Formula<String>> weakenFormulae = formula1.weakenFormula();
        for (Formula<String> formula : weakenFormulae) {
            formula.computePolarity();
            System.out.println(formula.getPolarity() + ", " + formula.toPLTLString());
        }
    }

    @Test
    public void strengthenFormulaTest() {
        Formula<String> formula1 = ParserUtils.parserPLTL("F(!(a & !G(b)) | c)");
        System.out.println(formula1.toPLTLString());
        formula1.computePolarity();
        Set<Formula<String>> subFormulae = formula1.getSubFormulas();
        for (Formula<String> formula : subFormulae) {
            System.out.println(formula.getPolarity() + ", " + formula.toPLTLString());
        }
        System.out.println();
        List<Formula<String>> strengthenFormulae = formula1.strengthenFormula();
        for (Formula<String> formula : strengthenFormulae) {
            formula.computePolarity();
            System.out.println(formula.getPolarity() + ", " + formula.toPLTLString());
        }
    }

    @Test
    public void strengthenFormulaForLevelTraversalTest() {
        Formula<String> formula1 = ParserUtils.parserPLTL("F(!(a & !G(b)) | c)");
        System.out.println(formula1.toPLTLString());
        formula1.computePolarity();
        Set<Formula<String>> subFormulae = formula1.getSubFormulas();
        for (Formula<String> formula : subFormulae) {
            System.out.println(formula.getPolarity() + ", " + formula.toPLTLString());
        }
        System.out.println();
        List<Formula<String>> strengthenFormulae = formula1.strengthenFormulaForLevelTraversal();
        for (Formula<String> formula : strengthenFormulae) {
            formula.computePolarity();
            System.out.println(formula.getPolarity() + ", " + formula.toPLTLString());
        }
    }
}
