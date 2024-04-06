package localsearch;

public enum InitializationType {
    /**
     * 随机单文字初始化
     */
    literal,
    /**
     * 随机单目标取反初始化
     */
    singleNotGoal,
    /**
     * 平凡解初始化
     */
    trivialBC,
    /**
     * 使用 GA 论文的初始化方法
     */
    GAInitialization;

    public static String allEnum() {
        String str = "";
        for (InitializationType type : InitializationType.values()) {
            str += type.toString() + ",";
        }
        str = str.substring(0, str.length()-1);
        return str;
    }
}
