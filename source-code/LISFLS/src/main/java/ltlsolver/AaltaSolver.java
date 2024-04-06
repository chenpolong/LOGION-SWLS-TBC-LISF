package ltlsolver;

import localsearch.BCSolution;
import ltlparse.Formula;
import main.InitialConfiguration;
import main.LogionState;
import utils.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AaltaSolver implements LTLSolver{
    final private String cmd = InitialConfiguration.aalta;

    List<String> doms;
    List<String> goals;
    String allDoms = null;
    String allGoals = null;

    protected AaltaSolver(List<Formula<String>> doms, List<Formula<String>> goals) {
        this.doms = new ArrayList<>();
        this.goals = new ArrayList<>();
        for (Formula<String> formula : doms) {
            this.doms.add(formula.toPLTLString());
        }
        for (Formula<String> formula : goals) {
            this.goals.add(formula.toPLTLString());
        }
        initialization();
    }

    private void initialization() {
        if (!doms.isEmpty()) {
            allDoms = "(" + String.join(") & (", doms) + ")";
        }

        if (!goals.isEmpty()) {
            allGoals = "(" + String.join(") & (", goals) + ")";
        }
    }

    @Override
    public LTLCheckResult _checkSAT(Formula<String> formula, long timeout) {
        return checkSATInvoke(formula.toPLTLString(), timeout);
    }

    @Override
    public BCCheckResult _checkInconsistency(Formula<String> candicateBC, long timeout) {
        String formula = candicateBC.toPLTLString() + " & " + allGoals;
        if (allDoms != null) {
            formula = formula + " & " + allDoms;
        }
//        System.out.println(formula);
        LTLCheckResult checkSATret = checkSATInvoke(formula, timeout);
        BCCheckResult ret = BCCheckResult.ERROR;
        switch (checkSATret) {
            case SAT:
                ret = BCCheckResult.NO;
                break;
            case UNSAT:
                ret = BCCheckResult.YES;
                break;
            case TIMEOUT:
                ret = BCCheckResult.TIMEOUT;
                break;
            case ERROR:
                ret = BCCheckResult.ERROR;
                break;
            case STOP:
                ret = BCCheckResult.STOP;
                break;
            default:
                ret = BCCheckResult.ERROR;
        }
        return ret;
    }

    @Override
    public BCCheckResult _checkMinimality(Formula<String> candicateBC, int index, long timeout) {
        if (index < 0 || index >= goals.size()) { return BCCheckResult.ERROR; }
        String formula = candicateBC.toPLTLString();
        if (allDoms != null) {
            formula = formula + " & " + allDoms;
        }
        for (int i = 0; i < goals.size(); i++) {
            if (i != index) {
                formula = formula + " & " + goals.get(i);
            }
        }
//        System.out.println(formula);
        LTLCheckResult checkSATret = checkSATInvoke(formula, timeout);
        BCCheckResult ret = BCCheckResult.ERROR;
        switch (checkSATret) {
            case SAT:
                ret = BCCheckResult.YES;
                break;
            case UNSAT:
                ret = BCCheckResult.NO;
                break;
            case TIMEOUT:
                ret = BCCheckResult.TIMEOUT;
                break;
            case ERROR:
                ret = BCCheckResult.ERROR;
                break;
            case STOP:
                ret = BCCheckResult.STOP;
                break;
            default:
                ret = BCCheckResult.ERROR;
        }
        return ret;
    }

    /**
     * 检查 trivialBC \not \model candicateBC, 即 \not candidateBC & trivialBC is SAT
     * @param candicateBC
     * @param timeout
     * @return
     */
    @Override
    public BCCheckResult _checkNonTriviality(Formula<String> candicateBC, long timeout) {
        String formula = "!" + candicateBC.toPLTLString() + " & !" + allGoals;

        LTLCheckResult checkSATret = checkSATInvoke(formula, timeout);
        BCCheckResult ret = BCCheckResult.ERROR;
        switch (checkSATret) {
            case SAT:
                ret = BCCheckResult.YES;
                break;
            case UNSAT:
                ret = BCCheckResult.NO;
                break;
            case TIMEOUT:
                ret = BCCheckResult.TIMEOUT;
                break;
            case ERROR:
                ret = BCCheckResult.ERROR;
                break;
            case STOP:
                ret = BCCheckResult.STOP;
                break;
            default:
                ret = BCCheckResult.ERROR;
        }
        return ret;
    }

    private LTLCheckResult checkSATInvoke(String formula, long timeout) {
        if (LogionState.searchStop) { return LTLCheckResult.STOP; }
        LTLCheckResult ret = LTLCheckResult.ERROR;
        boolean timeoutFlag = false;
        Process p = null;
        try  {
            p = IOUtils.invoke(cmd, formula);
            if (!p.waitFor(timeout, TimeUnit.SECONDS)) {
                timeoutFlag = true;
            }

            if (timeoutFlag) {
                ret = LTLCheckResult.TIMEOUT;
            } else {
                String aux;
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((aux = bufferedreader.readLine()) != null) {
//                    System.out.println(aux);
                    if (aux.equals("sat")) {
                        ret = LTLCheckResult.SAT;
                        break;
                    } else if (aux.equals("unsat")) {
                        ret = LTLCheckResult.UNSAT;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (p != null) {
                p.destroy();
            }
        }

        return ret;
    }


    public void addDomain(Formula<String> formula){
        String bc = formula.toPLTLString();
        this.doms.add(bc);
        this.allDoms = "(" + String.join(") & (", doms) + ")";
    }

}
