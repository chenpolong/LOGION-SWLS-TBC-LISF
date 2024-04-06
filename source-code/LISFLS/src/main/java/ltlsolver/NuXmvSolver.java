package ltlsolver;

import localsearch.BCSolution;
import ltlparse.Formula;
import main.InitialConfiguration;
import main.LogionState;
import utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NuXmvSolver implements LTLSolver {
    /**
     * 问题的所有变量集合
     */
    private List<String> variables;
    private String modelDir = InitialConfiguration.modelDir+File.separator;
    private List<Formula<String>> doms;
    private List<Formula<String>> goals;
    public static final String cmd = InitialConfiguration.nuXmv + " -dcx -int";

    protected NuXmvSolver(List<String> var, List<Formula<String>> doms, List<Formula<String>> goals) throws IOException, InterruptedException {
        variables = var;
        File dir = new File(modelDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        /**
         * universal.model
         */
        StringBuilder builder = new StringBuilder("MODULE main\nVAR\n");
        for (String v : variables) {
            builder.append("   " + v + ": boolean;\n");
        }

        IOUtils.fileWrite(builder.toString(), modelDir + "universal.model");

        /**
         * 初始化 inconsistency, minimality, non-triviality 所需要的 model
         */
        initialization(doms, goals);

        if(InitialConfiguration.usingCore){
            this.doms = new ArrayList<>();
            this.goals = new ArrayList<>();
            for (Formula<String> formula : doms) {
                this.doms.add(formula);
            }
            for (Formula<String> formula : goals) {
                this.goals.add(formula);
            }
        }

    }

    /**
     * 初始化 inconsistency, minimality, non-triviality 所需要的 model
     * @param doms
     * @param goals
     * @throws IOException
     * @throws InterruptedException
     */
    public void initialization(List<Formula<String>> doms, List<Formula<String>> goals) throws IOException, InterruptedException {
        Set<Formula<String>> s = new HashSet<>();
        String Command = "./ltl2smv 1 ";
        String tempFile = modelDir+"temp.txt";
        String file = modelDir+"module_inconsistency";

        /**
         * logical inconsistency module
         */
        s.addAll(doms);
        s.addAll(goals);
        String content = setToNuXmvString(s);
        if(content != null) {
            IOUtils.fileWrite(content, tempFile); //把需要输入的的东西放入文件temp.txt中
            Process p = Runtime.getRuntime().exec(Command + tempFile + " " + file); //执行命令
            p.waitFor();
            p.destroy();
            regulate(file); //规格化文件
        }

        /**
         * minimality module
         */
        int min_counter = 0;
        for (Formula<String> g : goals) {
            s.clear();
            s.addAll(doms);
            s.addAll(goals);
            s.remove(g);
            content = setToNuXmvString(s);
            IOUtils.fileWrite(content, tempFile);
            file = modelDir+"module_minimality" + "_" + (min_counter++);
            Process p = Runtime.getRuntime().exec(Command + tempFile + " " + file); //执行命令
            p.waitFor();
            p.destroy();
            regulate(file); //规格化文件
        }

        /**
         * non-triviality module
         */
        s.clear();
        boolean first = true;
        Formula<String> allgoals = null;
        for(Formula<String> g : goals){
            if (first){
                allgoals = g.clone();
                first = false;
            }
            else {
                allgoals = Formula.And(allgoals.clone(), g.clone());
            }
        }
        s.add(Formula.Not(allgoals));
        content = setToNuXmvString(s);
        IOUtils.fileWrite(content, tempFile);
        file = modelDir+"module_nonTriviality";
        Process p = Runtime.getRuntime().exec(Command + tempFile + " " + file); //执行命令
        p.waitFor();
        p.destroy();
        regulate(file); //规格化文件
    }

    private static String setToNuXmvString(Set<Formula<String>> formulas) {
        Formula<String> form = null;
        boolean first = true;
        for(Formula<String> f : formulas){
            if (first){
                form = f;
                first = false;
            }
            else {
                form = Formula.And(form, f);
            }
        }
        String formulaStr = form.toPLTLString();
        formulaStr = formulaStr.replaceAll("~", "!")
                .replaceAll("True","TRUE")
                .replaceAll("False","FALSE"); //格式改动
        return formulaStr;
    }

    /**
     * 添加模型的变量定义
     * @param file
     * @throws IOException
     */
    public void regulate(String file) throws IOException {
        BufferedReader bufferedReader = null;
        String line = null;
        StringBuffer buf = new StringBuffer(); //保存读入的内容
        bufferedReader = new BufferedReader(new FileReader(file));
        while( (line = bufferedReader.readLine()) != null ){
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
        IOUtils.fileWrite(content, file + ".model");

    }


    @Override
    public LTLCheckResult _checkSAT(Formula<String> formula, long timeout) {
        String formulaStr = "!( " + formula.toNuXmvLTL() + " )"; //取反
        String commando = "read_model -i "+modelDir+"universal.model\n" +
                "flatten_hierarchy\n" +
                "encode_variables\n" +
                "build_boolean_model\n" +
                "check_ltlspec_ic3 -d -p '" + formulaStr + "'\n" +
                "quit\n";
        return _checkSATWithCommando(commando, timeout);
    }

    @Override
    public BCCheckResult _checkInconsistency(Formula<String> candicateBC, long timeout) {
        String formula = "!( " + candicateBC.toNuXmvLTL() + " )"; //取反
        String file = modelDir+"module_inconsistency.model";
//        System.out.println(formula);
        String commando =
                "read_model -i" + file + "\n" +
                        "flatten_hierarchy\n" +
                        "encode_variables\n" +
                        "build_boolean_model\n" +
                        "check_ltlspec_ic3 -d -p \"" + formula + "\"\n" +
                        "quit\n";
        LTLCheckResult checkSATret = _checkSATWithCommando(commando, timeout);
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
        String formula = "!( " + candicateBC.toNuXmvLTL() + " )"; //取反
        String file = modelDir+"module_minimality_" + index + ".model";
        String commando =
                "read_model -i" + file + "\n" +
                        "flatten_hierarchy\n" +
                        "encode_variables\n" +
                        "build_boolean_model\n" +
                        "check_ltlspec_ic3 -d -p \"" + formula + "\"\n" +
                        "quit\n";
        LTLCheckResult checkSATret = _checkSATWithCommando(commando, timeout);
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

    @Override
    public BCCheckResult _checkNonTriviality(Formula<String> candicateBC, long timeout) {
        String formula = "" + candicateBC.toNuXmvLTL() + ""; //不取反
        String file = modelDir+"module_nonTriviality.model";
        String commando =
                "read_model -i" + file + "\n" +
                        "flatten_hierarchy\n" +
                        "encode_variables\n" +
                        "build_boolean_model\n" +
                        "check_ltlspec_ic3 -d -p \"" + formula + "\"\n" +
                        "quit\n";
        LTLCheckResult checkSATret = _checkSATWithCommando(commando, timeout);
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
     * 给定 nuXmv 的交互式输入命令,返回 LTLCheckResult 结果, 互式输入命令可以检查 BC的性质, 单个LTL 公式的可满足性
     * @param command
     * @param timeout
     * @return
     */
    private LTLCheckResult _checkSATWithCommando(String command, long timeout) {
        if (LogionState.searchStop) { return LTLCheckResult.STOP; }

        LTLCheckResult ret = LTLCheckResult.ERROR;
        Process p = null;
        boolean timeoutFlag = false;
        try {
            p = IOUtils.invoke(cmd, command);
            if (!p.waitFor(timeout, TimeUnit.SECONDS)) {
                timeoutFlag = true;
            }
            if (timeoutFlag) {
                ret = LTLCheckResult.TIMEOUT;
            } else {
                String aux;
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((aux = bufferedreader.readLine()) != null){
                    if( aux.contains("is true") ) {
                        ret = LTLCheckResult.UNSAT;
                        break;
                    } else if (aux.contains("is false")) {
                        ret = LTLCheckResult.SAT;
                        break;
                    } else if(aux.contains("Aborting batch mode")) {
                        throw new IllegalArgumentException("Invalid input for NuXmv.");
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

    @Override
    public void addDomain(Formula<String> formula) throws IOException, InterruptedException {
        if(InitialConfiguration.usingCore){
            this.doms.add(formula);
            initialization(this.doms, this.goals);
        }
    }
}
