package main;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 搜索到的所有BC都会保存到这里
 */
public class SolutionCollector {
    List<BCInfo> solutions = new LinkedList<>();

    public SolutionCollector() {}
    public List<BCInfo> getSolutions() {
        return solutions;
    }

    public void addBCInfo(BCInfo structure) {
        if (solutions.isEmpty()) {
            System.out.println(" >>> ["+ String.format("%.2f", structure.time) + " sec]" +
                    " First Solution:  value=" + String.format("%.2f", structure.value) + ", " + structure.bc);
        }
        solutions.add(structure);
    }

    public BCInfo bestBC() {
        BCInfo bestBcInfo = Collections.max(solutions, (b1, b2) -> {
            if (b1.getValue() > b2.getValue()) {
                return 1;
            } else if (b1.getValue() < b2.getValue()) {
                return -1;
            } else {
                return 0;
            }
        });
        return bestBcInfo;
    }
}
