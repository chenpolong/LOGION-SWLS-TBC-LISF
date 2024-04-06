package localsearch;

import ltlparse.Formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BCData {
    private List<Formula<String>> variables;

    private List<Formula.Content> contents;

    private void init() {
        contents = new ArrayList<>();
        contents.addAll(Arrays.asList(new Formula.Content[]{
                // Unary Operator
                Formula.Content.NOT,
                Formula.Content.NEXT,
                Formula.Content.GLOBALLY,
                Formula.Content.FUTURE,
                // Binary Operator
                Formula.Content.AND,
                Formula.Content.OR,
                //Formula.Content.IFF, // 弱化操作不支持
                Formula.Content.UNTIL,
                Formula.Content.WEAK_UNTIL,
                // no support Release
        }));
    }

    public BCData(List<Formula<String>> variable) {
        this.variables = variable;
        init();
    }

    public List<Formula<String>> getVariables() {
        return variables;
    }

    public List<Formula.Content> getContents() {
        return contents;
    }
}
