package utils.ABC;


import de.uni_luebeck.isp.rltlconv.automata.Nba;
import main.InitialConfiguration;
import main.LogionState;
import main.StatisticState;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ABCModelCounter {
    private String alpha;
    private List<String> formulalist;
    BigInteger domModelCount = null;
    int bound;
    public ABCModelCounter(String alpha, List<String> domStr, int bound) {
        this.alpha = alpha;
        this.formulalist = domStr;
        this.bound = bound;
    }

    public void initialization() {
        this.domModelCount = countInvoke(formulalist, alpha, this.bound, InitialConfiguration.modelCountingTimeout);
    }


    public BigInteger count(String formula)  {
        /**
         * 如果模型计数计算基本例子超时，则关闭这个功能
         */
        if (!LogionState.canModelCounting) {
            return new BigInteger("0");
        }

        formulalist.add(formula);
        BigInteger ret = countInvoke(formulalist, alpha, bound, InitialConfiguration.modelCountingTimeout);
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
        if (LogionState.searchStop) { return BigInteger.ZERO; }

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
        long startTime = System.currentTimeMillis();
        Thread thread = new Thread(future);
        thread.start();
        LinkedList<String> abcStrs = null;
        PrintStream psOld = System.out;
        try {
            abcStrs = future.get(timeout_millisecond, TimeUnit.MILLISECONDS);
            long duringTime = System.currentTimeMillis() - startTime;
            timeout_millisecond -= duringTime;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            thread.interrupt();
            System.setOut(psOld); // 恢复原来的输出路径
            return count;
        }
        long duringTime = System.currentTimeMillis() - startTime;
        timeout_millisecond -= duringTime;

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

    public BigInteger getDomModelCount() {
        if (domModelCount == null) {
            initialization();
        }
        return domModelCount;
    }

    Integer scale = null;
    public int domainModelScale() {
        if (domModelCount == null) {
            initialization();
        }
        if (scale == null) {
            scale = domModelCount.toString().length();
        }
        return scale;
    }
}
