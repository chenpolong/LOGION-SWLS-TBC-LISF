package ltlsolver;

import ltlparse.Formula;
import utils.ParserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class LTLSolverFactory {
    static AaltaSolver aaltaSolver = null;
    static PltlSolver pltlSolver = null;
    static NuXmvSolver nuXmvSolver = null;
    static LISFSolver lisfSolver = null;

    List<Formula<String>> doms;
    List<Formula<String>> goals;

    public LTLSolver getSolver(LTLSolverType type) {
        if (type == LTLSolverType.aalta) {
            return initAaltaSolver();
        } else if (type == LTLSolverType.ptlt) {
            return initPltlSolver();
        } else if (type == LTLSolverType.nuXmv) {
            return initNuXmvSolver();
        } else if (type == LTLSolverType.LISF) {
            return initLISFSolver();
        } else {
            throw new IllegalArgumentException("has not such ltl solver: type=" + type);
        }
    }

    public LTLSolverFactory(List<Formula<String>> doms, List<Formula<String>> goals) {
        this.doms = doms;
        this.goals = goals;
    }

    protected AaltaSolver initAaltaSolver() {
        if (aaltaSolver == null) {
            aaltaSolver = new AaltaSolver(doms, goals);
        }
        return aaltaSolver;
    }

    protected PltlSolver initPltlSolver() {
        if (pltlSolver == null) {
            pltlSolver = new PltlSolver(doms, goals);
        }
        return pltlSolver;
    }

    protected NuXmvSolver initNuXmvSolver() {
        if (nuXmvSolver == null) {
            List<String> ltls = new ArrayList<>();
            for (Formula<String> formula : doms) {
                ltls.add(formula.toPLTLString());
            }
            for (Formula<String> formula : goals) {
                ltls.add(formula.toPLTLString());
            }
            Set<String> vars = ParserUtils.getVariables(ltls);

            try {
                nuXmvSolver = new NuXmvSolver(new ArrayList<>(vars), doms, goals);
            } catch (Exception e) {
                throw new RuntimeException("construct nuXmvSolver error, " + e);
            }
        }
        return nuXmvSolver;
    }

    protected LISFSolver initLISFSolver() {
        if (lisfSolver == null) {
            try {
                lisfSolver = new LISFSolver(doms, goals);
            } catch (Exception e) {
                throw new RuntimeException("construct LISFSolver error, " + e);
            }
        }
        return lisfSolver;
    }
}
