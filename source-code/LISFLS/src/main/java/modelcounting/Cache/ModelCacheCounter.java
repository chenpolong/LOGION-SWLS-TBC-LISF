package modelcounting.Cache;

import ltlparse.Formula;
import ltlsolver.LTLCheckResult;
import main.InitialConfiguration;
import modelcounting.ModelCounter;
import utils.Pair;
import utils.Solver;

import java.io.File;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO 如果两个BC得分相同, 才会使用ModelCache比较优劣
 */

public class ModelCacheCounter<T> implements ModelCounter {


    static private final String traceParentPath = InitialConfiguration.tempDir;
    static private final String modelPath       = InitialConfiguration.modelDir;

    private int directoryTypeNum = 3;
    private LassoCache[] lassoCaches;
    private int maxLassoCache = 30;
    private Queue<Pair<String, String>> lassoFiles = new LinkedList<>();


    private class LassoCache<T>{
        private String directoryType;
        private int totalNum;
        private LassoCache(String directoryType, int totalNum){
            this.directoryType = directoryType;
            this.totalNum = totalNum;
        }
        private int getTotalNum(){
            return totalNum;
        }
        private void addTotalNum(){
            this.totalNum++;
        }
    }

    public ModelCacheCounter(int goalNum, int maxLassoCache){
        this.maxLassoCache = maxLassoCache;
        directoryTypeNum = goalNum+2;
        lassoCaches = new LassoCache[directoryTypeNum];
        lassoCaches[0] = new LassoCache("inconsistency", 0);
        lassoCaches[1] = new LassoCache("nonTriviality", 0);
        for(int i = 0; i < goalNum; ++i){
            lassoCaches[i+2] = new LassoCache("minimality_-"+i, 0);
        }
    }

    public int getTotalLassoNum(){
        int totalLasso = 0;
        for(int i = 0; i < directoryTypeNum; ++i){
            totalLasso += lassoCaches[i].getTotalNum();
        }
        return totalLasso;
    }

    public int getValidLassoNum(){
        return lassoFiles.size();
    }

    public void addLasso(String directoryType, int traceIndex) {
        String lassFile = "traces_" + traceIndex + ".xml";
        if(lassoFiles.size() >= maxLassoCache){
            lassoFiles.poll();
        }
        lassoFiles.offer(new Pair<>(directoryType, lassFile));
        int typeIndex = getIndexByType(directoryType);
        lassoCaches[typeIndex].addTotalNum();

    }

    private int getIndexByType(String type){
        if(type.equals("inconsistency")){
            return 0;
        }
        if(type.equals("nonTriviality")){
            return 1;
        }
        else{
            return Integer.parseInt(type.substring(12));
        }
    }

    @Override
    public BigInteger count(Formula<String> BC) {
//        double count = 0;
        BigInteger count = new BigInteger(String.valueOf(0));
        for(Pair<String, String> lasso : lassoFiles){
            String description = lasso.getKey();
            String lassoFile = lasso.getValue();
            String traceFilePath = traceParentPath+description+File.separator+lassoFile;
            String universalModel = modelPath + "universal_" + description + ".model";
            String formula = BC.toNuXmvLTL();
            LTLCheckResult ret = Solver.pathCheckingByNuxmv(universalModel, traceFilePath, formula);
            if(ret == LTLCheckResult.SAT){
                count.add(new BigInteger(String.valueOf(1)));
//                count++;
            }
        }
//        System.out.println("===== check count: "+count+" totallasso: "+getTotalLassoNum()+" validlasso: "+getValidLassoNum());
        return count;
    }

}
