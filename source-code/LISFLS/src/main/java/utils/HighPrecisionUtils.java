package utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class HighPrecisionUtils {
    /**
     * 高精度计算 bigInteger1 / bigInteger2， 保留 scale 有效数字
     * 除0 会抛出 ArithmeticException 异常
     * @param bigInteger1
     * @param bigInteger2
     * @param scale
     * @return
     */
    public static BigDecimal bigIntegerDivide(BigInteger bigInteger1, BigInteger bigInteger2, int scale) {
        BigDecimal d1 = new BigDecimal(bigInteger1);
        BigDecimal d2 = new BigDecimal(bigInteger2);
        BigDecimal ret = d1.divide(d2, scale, BigDecimal.ROUND_CEILING);
        return ret;
    }
}
