package localsearch;

import ltlparse.Formula;
import org.jamesframework.core.problems.sol.Solution;

import java.util.Objects;

public final class BCSolution extends Solution {
    private Formula<String> formula;

    public BCSolution(Formula<String> f) {
        formula = f;
    }

    public Formula<String> getFormula() {
        return formula;
    }

    public void setFormula(Formula<String> formula) {
        this.formula = formula;
    }

    @Override
    public Solution copy() {
        return new BCSolution(formula.clone());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BCSolution other = (BCSolution) obj;
        return this.formula.equals(other.formula);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(formula);
    }

    @Override
    public String toString() {
        return "BCSolution{" +
                "formula=" + formula +
                '}';
    }

    public void swap(BCSolution solution) {
        Formula<String> temp = solution.formula;
        solution.formula = this.formula;
        this.formula = temp;
    }
}
