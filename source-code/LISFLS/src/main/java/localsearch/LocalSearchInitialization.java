package localsearch;

import ltlparse.Formula;
import utils.ParserUtils;
import utils.Polling;

import java.util.*;

/**
 * 初始化方法类， 实现  {@link InitializationType} 定义的每一个种初始化方法
 */
public class LocalSearchInitialization {
    static public Polling<Formula<String>> initialization(List<Formula<String>> doms, List<Formula<String>> goals, InitializationType type) {
        Polling<Formula<String>> polling = null;
        if (type == InitializationType.literal) {
            List<String> ltls = new ArrayList<>();
            for (Formula<String> formula : doms) {
                ltls.add(formula.toPLTLString());
            }
            for (Formula<String> formula : goals) {
                ltls.add(formula.toPLTLString());
            }
            Set<String> vars = ParserUtils.getVariables(ltls);

            List<Formula<String>> literals = new ArrayList<>();
            for (String var : vars) {
                literals.add(ParserUtils.parserPLTL(var));
                literals.add(Formula.Not(ParserUtils.parserPLTL(var)));
            }
            polling = new Polling<>(literals);
        } else if (type == InitializationType.singleNotGoal)  {
            List<Formula<String>> gs = new ArrayList<>();
            for (Formula<String> goal : goals) {
                gs.add(Formula.Not(goal));
            }
            polling = new Polling<>(gs);
        } else if (type == InitializationType.trivialBC) {
            boolean first = true;
            Formula<String> trivialBC = null;
            for (Formula<String> gs : goals) {
                if (first) {
                    trivialBC = gs;
                    first = false;
                } else {
                    trivialBC  = Formula.And(trivialBC, gs);
                }
            }
            polling = new Polling<>(Arrays.asList(Formula.Not(trivialBC)));
        } else if (type == InitializationType.GAInitialization) {
            List<String> lists = getInitialPopulation(doms, goals);
            List<Formula<String>> formulaList = new LinkedList<>();
            for (String str : lists) {
                formulaList.add(ParserUtils.parserPLTL(str));
            }
            polling = new Polling<>(formulaList);
        } else {
            throw new IllegalArgumentException("initialization error: " + type);
        }
        return polling;
    }

    static private List<String> getInitialPopulation(List<Formula<String>> doms, List<Formula<String>>goals) {
        List<String> ret = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (Formula<String> formula : doms) {
            for (Formula<String> subformula : formula.getSubFormulas()) {
                set.add(subformula.toPLTLString());
            }
        }
        for (Formula<String> formula : goals) {
            for (Formula<String> subformula : formula.getSubFormulas()) {
                set.add(subformula.toPLTLString());
            }
        }
        for (String str : set) {
            ret.add(str);
            ret.add("!" +str);
        }
        return ret;
    }
}
