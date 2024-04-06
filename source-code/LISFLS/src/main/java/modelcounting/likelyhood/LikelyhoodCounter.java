package modelcounting.likelyhood;

import de.uni_luebeck.isp.rltlconv.automata.Nba;
import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import main.InitialConfiguration;
import main.LogionState;
import main.StatisticState;
import modelcounting.ModelCounter;
import utils.ABC.ABC;
import utils.ABC.LTLModelCountHelper;
import utils.HighPrecisionUtils;
import utils.ParserUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LikelyhoodCounter implements ModelCounter {
    private final int K                             = InitialConfiguration.modelCountingBound;
    private List<String> formulalist                = new ArrayList<>();
    private String alpha;

    public LikelyhoodCounter() throws ParseErrorException {
//        long startTime = System.currentTimeMillis();
        Set<String> allvar = ParserUtils.getVariables(LogionState.doms, LogionState.goals);
        StringBuilder alpha = new StringBuilder();
        alpha.append("[");
        boolean first = true;
        for (String str : allvar) {
            Formula<String> var = Parser.parse(str);
            if (first) {
                alpha.append(var.toRLTL().replace("(", "").replace(")", "").trim());
                first = false;
            } else {
                alpha.append("," + var.toRLTL().replace("(", "").replace(")", "").trim());
            }
        }
        alpha.append("]");
        for (String str : LogionState.doms) {
            Formula<String> formula = Parser.parse(str);
            formulalist.add(formula.toRLTL());
        }
        this.alpha = alpha.toString();
        /**
         * 表示模型计数无法在超时时间内计算模型个数
         */
//        modelCounter.initialization();
//        if (modelCounter.getDomModelCount().equals(BigInteger.ZERO)) { canModelCounting = false; }
//        StatisticState.modelCounterStartTime = System.currentTimeMillis() - startTime;
    }
    @Override
    public BigInteger count(Formula<String> BC) {
        formulalist.add(BC.toRLTL());
        BigInteger ret = countInvoke(formulalist, alpha, K, InitialConfiguration.modelCountingTimeout);
        formulalist.remove(formulalist.size()-1);
        return ret;
    }

    protected BigInteger countInvoke(List<String> formulas, String alph, long bound, int timeout) {
        StatisticState.callModelCounterCount++;
        long time = System.currentTimeMillis();
        BigInteger ret = _countInvoke(formulas, alph, bound, timeout);
        StatisticState.callModelCounterTime += (System.currentTimeMillis() - time);
        return ret;
    }

    protected BigInteger _countInvoke(List<String> formulas, String alph, long bound, int timeout) {
//        if (LogionState.searchStop) {
//            StatisticState.modelCounterZeroCount++;
//            return BigInteger.ZERO;
//        }
        BigInteger count = BigInteger.ZERO;
        long timeout_millisecond = timeout * 1000;

        /**
         * construct future work
         */
        LTLModelCountHelper helper = new LTLModelCountHelper();
        FutureTask<LinkedList<String>> future = new FutureTask<>(() -> {
            LinkedList<String> abcStrs = new LinkedList<>();
            for (String formula : formulas) {
                if (LogionState.searchStop) { return abcStrs; }
                if (Thread.currentThread().isInterrupted()) { break; }
                String abcStr = genABCString(formula, alph, helper);
                abcStrs.add(abcStr);
            }
            return abcStrs;
        });

        /**
         * generate ABC input
         */
//        long startTime = System.currentTimeMillis();
        Thread thread = new Thread(future);
        thread.start();
        LinkedList<String> abcStrs = null;
        PrintStream psOld = System.out;
        try {
            abcStrs = future.get(timeout_millisecond, TimeUnit.MILLISECONDS);
//            long duringTime = System.currentTimeMillis() - startTime;
//            timeout_millisecond -= duringTime;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            thread.interrupt();
            System.setOut(psOld); // 恢复原来的输出路径
            return count;
        }
//        long duringTime = System.currentTimeMillis() - startTime;
//        timeout_millisecond -= duringTime;

        /**
         * call ABC
         */
        if (helper.encoded_alphabet) { count = ABC.count(abcStrs, bound*2, timeout_millisecond); }
        else { count = ABC.count(abcStrs, bound*2, timeout_millisecond); }
        return count;
    }

    protected String genABCString(String ltl, String alph, LTLModelCountHelper helper) {

        helper.encoded_alphabet = false;
        String form = "LTL="+ltl;
        if(alph!=null && alph!="")
            form += ",ALPHABET="+alph;
        if(alph.split(",").length>5)
            helper.encoded_alphabet = true;
        Nba nba = helper.ltl2nba(form);
        if (Thread.currentThread().isInterrupted()) { return ""; }
        String s = helper.automata2RE(nba);
        if (Thread.currentThread().isInterrupted()) { return ""; }
        return helper.toABClanguage(s);
    }

}
