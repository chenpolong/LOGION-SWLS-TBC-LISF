package modelcounting;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;
import main.InitialConfiguration;
import main.LogionState;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestABCModelCounter {
    @Before
    public void initialition() throws ParseErrorException, IOException, InterruptedException {
        List<String> doms = Arrays.asList(
                "[]((p && X(p)) -> X(X(! h)))"
        );
        List<String> goals = Arrays.asList(
                "[](h -> X(p))",
                "[](m -> X(! p))"
        );

        List<Formula<String>> domsformula = new ArrayList<>();
        List<Formula<String>> goalsformula = new ArrayList<>();
        for (String str : doms) {
            domsformula.add(Parser.parse(str));
        }
        for (String str : goals) {
            goalsformula.add(Parser.parse(str));
        }

        InitialConfiguration.usingModelCounting = true;
        LogionState.initalization(doms, goals);
    }

//    @Test
//    public void testInitialzation() {
//        ABCModelCounter ABCModelCounter = LogionState.modelCounter;
//        System.out.println("#{Doms, " + InitialConfiguration.modelCountingBound + "}: " + ABCModelCounter.getDomModelCount());
//        System.out.println("model counter initial time: " +
//                String.format("%.2f", (StatisticState.modelCounterStartTime/1000.0)));
//    }
//
//    @Test
//    public void testCount() {
//        ABCModelCounter ABCModelCounter = LogionState.modelCounter;
//
//        String bc = "F(h & m)";
//        System.out.println("bc: " + bc);
//        Formula<String> formula = ParserUtils.parserPLTL(bc);
//        BigInteger ret = ABCModelCounter.count(formula.toRLTL());
//        System.out.println("#{Doms, bc}: " + ret);
//        System.out.println("model counter time: " +
//                String.format("%.2f", (StatisticState.callModelCounterTime/1000.0)));
//    }
}


