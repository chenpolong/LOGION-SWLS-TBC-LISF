package ltlsolver;

public enum LTLSolverType {
    aalta,
    nuXmv,
    ptlt,
    LISF;

    public static String allEnum() {
        String str = "";
        for (LTLSolverType type : LTLSolverType.values()) {
            str += type.toString() + ",";
        }
        str = str.substring(0, str.length()-1);
        return str;
    }
}
