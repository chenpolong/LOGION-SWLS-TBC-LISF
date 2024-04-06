package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 自定义 [0, n) 内每个整数的权重,并返回按照该分布采样的随机数
 * 实现：将每个数的权重归一化后,映射到 [0, 100] 数轴上
 */
public class DiscreteDistribution {
    private List<Double> weight = new ArrayList();
    public DiscreteDistribution(List<Double> weight) {
        double totalWeight = 0;
        for (Double val : weight) {
            totalWeight += val;
        }

        if (Math.abs(totalWeight) < 10E-6) {
            throw new RuntimeException("构造函数的输入权重不能全为0"+ weight.toString());
        }

        double accumul = 0;
        for (Double val : weight) {
            accumul += val;
            this.weight.add(100.0 * accumul / totalWeight);
        }
    }

    public int getNext(Random rnd) {
        double sampleValue = rnd.nextDouble()*100;
        int size = weight.size();
        for (int i = 0; i < size; i++) {
            if (sampleValue <= weight.get(i)) {
                return i;
            }
        }
        return size - 1;
    }
}
