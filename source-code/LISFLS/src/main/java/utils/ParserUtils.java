package utils;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlparse.Parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {
    static final Pattern varPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]+)|([a-z])");

    static Set<String> extendVars = new HashSet();

    static {
        extendVars.add("TRUE");
        extendVars.add("True");
        extendVars.add("true");
        extendVars.add("FALSE");
        extendVars.add("False");
        extendVars.add("false");
    }

    static public Set<String> getVariables(List<String> ltls) {
        Set<String> vars = new HashSet<>();

        for (String ltl : ltls) {
            Matcher matcher   = varPattern.matcher(ltl);
            while (matcher.find()) {
                vars.add(matcher.group());
            }
        }

        Iterator<String> it = vars.iterator();
        while (it.hasNext()) {
            String v = it.next();
            if (extendVars.contains(v)) {
                it.remove();
            }
        }

        return vars;
    }

    static public Set<String> getVariables(List<String> doms, List<String> goals) {
        List<String> ltls = new ArrayList<>(doms);
        ltls.addAll(goals);
        return getVariables(ltls);
    }

    static public Formula<String> parserPLTL(String ltl) {
        Formula<String> formula = null;
        ltl = ltl.replaceAll("\\bG\\b", "[]")
                .replaceAll("\\bF\\b", "<>")
                .replace("&", "&&")
                .replace("|", "||")
                .replace("~", "!")
                .replace("True", "true")
                .replace("False", "false")
                .replace("TRUE", "true")
                .replace("FALSE", "false");
        try {
            formula = Parser.parse(ltl);
        } catch (ParseErrorException e) {
            throw new IllegalArgumentException("error pltl: " + ltl, e);
        }
        return formula;
    }
}
