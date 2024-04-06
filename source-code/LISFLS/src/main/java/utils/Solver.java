package utils;

import ltlsolver.LTLCheckResult;
import main.InitialConfiguration;
import main.LogionState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * 静态方法：判断某ltl公式是否可满足
 */
public class Solver {
    static private final String nuXmvCmd        = InitialConfiguration.nuXmv + " -int";
    static public long PATHMAXTIME              = InitialConfiguration.LISFModelCheckAPathTimeout;

    public static LTLCheckResult pathCheckingByNuxmv(String universalModel, String traceFilePath, String formula){
        if (LogionState.searchStop) { return LTLCheckResult.STOP; }
        final String mcPathCmd = "read_model -i '" +universalModel+  "'\n" +
                "flatten_hierarchy\n" +
                "encode_variables\n" +
                "build_boolean_model\n" +
                "read_trace " +traceFilePath+ "\n" +
                "bmc_setup\n" +
                "check_ltlspec_on_trace -p '" +formula+ "' 1\n" +
                "quit\n";

        LTLCheckResult ret = LTLCheckResult.ERROR;
        try {
            Process p = IOUtils.invoke(nuXmvCmd, mcPathCmd);

            boolean timeoutFlag = false;
            if ( !p.waitFor(PATHMAXTIME, TimeUnit.SECONDS) ) {
                timeoutFlag = true;
                p.destroy();
            }
            if (timeoutFlag) {
                ret = LTLCheckResult.TIMEOUT;
                p.destroy();
            } else {
                String aux;
                String line = "";

                InputStream in = p.getInputStream();
                InputStreamReader inread = new InputStreamReader(in);
                BufferedReader bufferedreader = new BufferedReader(inread);
                while ((aux = bufferedreader.readLine()) != null) {
                    if (aux.startsWith("The property")) {
                        line = aux;
                        if (line.contains("is satisfied")) {
                            ret = LTLCheckResult.SAT;
                        } else if (line.contains("is not satisfied")) {
                            ret = LTLCheckResult.UNSAT;
                        } else {
                            ret = LTLCheckResult.ERROR;
                        }
                        break;
                    }
                }

//                InputStream err = p.getErrorStream();
//                InputStreamReader errread = new InputStreamReader(err);
//                BufferedReader errBufferedReader = new BufferedReader(errread);
//                while((aux = errBufferedReader.readLine()) != null){
//                    System.err.println();
//                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
