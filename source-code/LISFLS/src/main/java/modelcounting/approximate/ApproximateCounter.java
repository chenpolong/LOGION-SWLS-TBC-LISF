package modelcounting.approximate;

import ltlparse.Formula;
import main.InitialConfiguration;
import main.LogionState;
import main.StatisticState;
import modelcounting.ModelCounter;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApproximateCounter implements ModelCounter {

    private List<String> doms = new ArrayList<>();
    private List<String> variables = new ArrayList<>();

    static final String tempDir = "./temp/";
    static final String output = tempDir+"boundedmodelcounting";
    static final String modelDir = "./model/";
    static final String domainFile = modelDir + "domain.model";
    static final String indgcnf = output+".dimacs.ind.gcnf";
    static final String togmusCmd = "./togmus " +  output+".dimacs " + indgcnf + " False";
    static final String nuXmvcmd = "./nuXmv -dcx -int";

    /**
     * milliseconds
     */
    static public long boundedModelCheckingTime = 0;
    static public long independenceSetTime = 0;
    static public long approxSATModelCountingTime = 0;


    static final String approxMCCmd = "./approxmc " + output+".dimacs";
    static final Pattern approxmcPattern  = Pattern.compile("\\d+");

    public ApproximateCounter() throws IOException, InterruptedException {
        for(Formula<String> dom : LogionState.formulaDoms){
            this.doms.add(dom.toNuXmvLTL());
        }
        for(String var : LogionState.vars){
            this.variables.add(var);
        }

        /**
         * 创建临时文件夹保存文件
         */
        File dir = new File(modelDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        initiate(doms);
    }

    @Override
    public BigInteger count(Formula<String> BC) {
        BigInteger ret = count(BC.toNuXmvLTL(), InitialConfiguration.modelCountingTimeout);
        return ret;
    }


    private void initiate(List<String> doms) throws IOException, InterruptedException {
        String Command = "./ltl2smv 1 ";
        String tempFile = modelDir+"approximate_model_counter_temp.txt";
        String file = modelDir+"domain";

        /**
         * domain
         */
        String content = listToNuXmvString(doms);
        if(!doms.isEmpty()) {
            fileWrite(content, tempFile); //把需要输入的的东西放入文件approximate_model_counter_temp.txt中
            Process p = Runtime.getRuntime().exec(Command + tempFile + " " + file); //执行命令
            p.waitFor();
            p.destroy();
            regulate(file); //规格化文件
        }
    }

    private void fileWrite(String content, String file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter bufout = new OutputStreamWriter(out);
        BufferedWriter bufferedwriter = new BufferedWriter(bufout, content.getBytes().length+1);
        bufferedwriter.write(content);
        bufferedwriter.close();
        bufout.close();
        out.close();
    }

    private static String listToNuXmvString(List<String> formulas) {
        String formulaStr = "";
        boolean first = true;
        for (String formula : formulas) {
            if (first) {
                formulaStr = "("+formula+")";
                first = false;
            } else {
                formulaStr += "& (" + formula + ")";
            }
        }

        formulaStr = formulaStr.replaceAll("~", "!")
                .replaceAll("True","TRUE")
                .replaceAll("False","FALSE"); //格式改动
        return formulaStr;
    }

    private void regulate(String file) throws IOException {
        BufferedReader bufferedReader = null;
        String line = null;
        StringBuffer buf = new StringBuffer(); //保存读入的内容
        bufferedReader = new BufferedReader(new FileReader(file));
        while((line = bufferedReader.readLine()) != null ){
            if(line.startsWith("MODULE"))
                buf.append("MODULE main\n");
            else if (line.startsWith("VAR")){
                String var = "VAR\n";
                for( String s: variables){
                    var += s + " :boolean;\n";
                }
                buf.append(var);
            }
            else
                buf.append(line + "\n");
        }  //修改文件
        bufferedReader.close();

        String content = buf.toString();
        fileWrite(content, domainFile);
    }




    protected long encodingSATProblem(String formula, long timeout) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();

        final String nuXmvSource = "read_model -i " + domainFile + "\n" +
                "flatten_hierarchy\n" +
                "encode_variables\n" +
                "build_boolean_model\n" +
                "bmc_setup\n" +
                "check_ltlspec_bmc -o "+ output + " -p \"" + formula + "\" -k 10\n" +
                "quit\n";

        Process p = Runtime.getRuntime().exec(nuXmvcmd); //执行命令
        OutputStream o = p.getOutputStream();
        OutputStreamWriter ou = new OutputStreamWriter(o);
        BufferedWriter OUT = new BufferedWriter(ou);
        OUT.write(nuXmvSource);
        OUT.close();
        ou.close();
        o.close();

        boolean timeoutFlag = false;
        if(!p.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            timeoutFlag = true; //kill the process.
            p.destroy(); // consider using destroyForcibly instead
        }

        long duringTime =  System.currentTimeMillis()-startTime;

        if (timeoutFlag) {
            p.destroy();
        }
        return duringTime;
    }


    protected long callTogmus(long timeout) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();

        Process p = Runtime.getRuntime().exec(togmusCmd); //执行命令

        boolean timeoutFlag = false;
        if(!p.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            timeoutFlag = true; //kill the process.
            p.destroy(); // consider using destroyForcibly instead
        }

        long duringTime = System.currentTimeMillis()-startTime;

        if (timeoutFlag) {
            p.destroy();
        }
        return duringTime;
    }



    class Pair<A, B> {
        public final A a;
        public final B b;
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }


    protected Pair<String, Long> callMuser2(long timeout) throws IOException, InterruptedException {
        final String muser2Cmd = "./muser2 -v 0 -grp -comp -minisats -order 4 -T " + timeout/2000 + " " + indgcnf;
        long startTime = System.currentTimeMillis();

        Process p = Runtime.getRuntime().exec(muser2Cmd); //执行命令
        boolean timeoutFlag = false;
        if(!p.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            timeoutFlag = true; //kill the process.
            p.destroy(); // consider using destroyForcibly instead
        }
        long duringTime = System.currentTimeMillis()-startTime;

        if (timeoutFlag) {
            p.destroy();
            return new Pair<>("", duringTime);
        } else {
            String aux;
            String ret = "";
            InputStream in = p.getInputStream();
            InputStreamReader inread = new InputStreamReader(in);
            BufferedReader bufferedreader = new BufferedReader(inread);
            while ((aux = bufferedreader.readLine()) != null) {
                if (aux.startsWith("v")) {
                    ret = aux;
                    break;
                }
            }
            return new Pair<>(ret, duringTime);
        }
    }


    protected Pair<BigInteger, Long> callApproxMC(long timeout) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec(approxMCCmd); //执行命令
        boolean timeoutFlag = false;
        if(!p.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            timeoutFlag = true; //kill the process.
            p.destroy(); // consider using destroyForcibly instead
        }
        long duringTime = System.currentTimeMillis()-startTime;

        if (timeoutFlag) {
            p.destroy();
            return new Pair<>(BigInteger.ZERO, duringTime);
        } else {
            BigInteger ret = BigInteger.ZERO;

            String aux;
            InputStream in = p.getInputStream();
            InputStreamReader inread = new InputStreamReader(in);
            BufferedReader bufferedreader = new BufferedReader(inread);
            while ((aux = bufferedreader.readLine()) != null) {
                if (aux.startsWith("[appmc] Number of solutions is:")) {
                    Matcher matcher   = approxmcPattern.matcher(aux);
                    List<String> list = new ArrayList<>();
                    while (matcher.find()) { list.add(matcher.group()); }
                    if (list.size() != 3) {
                        throw new IOException("approxmc output: " + aux);
                    }

                    /**
                     * generate result
                     */
                    StringBuilder exponention = new StringBuilder();
                    exponention.append('1');
                    for (int i = 0; i < Integer.parseInt(list.get(2)); i++) {
                        exponention.append('0');
                    }
                    ret = new BigInteger(exponention.toString(), 2);
                    ret = ret.multiply(new BigInteger(list.get(0)));
                    break;
                }
            }
            return new Pair<>(ret, duringTime);
        }
    }

        /**
         *
         * @param formula
         * @param timeout: second. timeout <= 0 means unlimited;
         * @return
         */
        public BigInteger count(String formula, int timeout) {
            BigInteger ret = BigInteger.ZERO;
            if (formula == null || LogionState.searchStop) { return ret; }
            StatisticState.callModelCounterCount++;
            long lefttime = timeout * 1000;
            if (timeout <= 0) { lefttime = Integer.MAX_VALUE - 100; }

            try {
                /**
                 * call encode SAT problem using bounded model checking by NuXmv
                 */
                long duringtime = encodingSATProblem(formula, lefttime);
                lefttime -= duringtime;
                boundedModelCheckingTime += duringtime;

                /**
                 * call togmus and muser2 to compute independent set
                 */
                long beforetime = lefttime;
                duringtime = callTogmus(lefttime);
                lefttime -= duringtime;

                Pair<String, Long> pair = callMuser2(lefttime);
                String ind = pair.a.replaceAll("v", "c ind");
                duringtime = pair.b;
                lefttime -= duringtime;

                /**
                 * ${ind} append to ${output}.dimacs
                 */
                FileWriter writer = new FileWriter(output + ".dimacs", true);
                writer.write(ind);
                writer.close();
                independenceSetTime += beforetime - lefttime;

                /**
                 * call approxMC to compute model count
                 */
                Pair<BigInteger, Long> bigIntegerLongPair = callApproxMC(lefttime);
                ret = bigIntegerLongPair.a;
                duringtime = bigIntegerLongPair.b;
                approxSATModelCountingTime += duringtime;
                StatisticState.callModelCounterTime += duringtime;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            if(ret == BigInteger.ZERO){
                StatisticState.modelCounterZeroCount++;
            }
            return ret;
        }

}
