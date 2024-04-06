package modelcounting;

import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import main.InitialConfiguration;
import main.LogionState;
import modelcounting.Cache.ModelCacheCounter;
import modelcounting.approximate.ApproximateCounter;
import modelcounting.likelyhood.LikelyhoodCounter;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.List;

public class ModelCounterFactory {

    ModelCounterType modelCounterType   = ModelCounterType.CACHE;
    static ModelCacheCounter modelCacheCounter = null;
    static LikelyhoodCounter likelyhoodCounter = null;
    static ApproximateCounter approximateCounter = null;

    public void initialize(ModelCounterType modelCounterType) throws ParseErrorException, IOException, InterruptedException {
        this.modelCounterType = modelCounterType;
        if(modelCounterType == ModelCounterType.CACHE){
            modelCacheCounter = new ModelCacheCounter(LogionState.formulaGoals.size(), InitialConfiguration.modelCountingBound);
        }
        else if(modelCounterType == ModelCounterType.LIKELYHOOD){
            likelyhoodCounter = new LikelyhoodCounter();
        }
        else if(modelCounterType == ModelCounterType.APPROXIMATE){
            approximateCounter = new ApproximateCounter();
        }
    }

    public ModelCounter getCounter(){
        if(this.modelCounterType == ModelCounterType.CACHE){
            return modelCacheCounter;
        }
        if(this.modelCounterType == ModelCounterType.LIKELYHOOD){
            return likelyhoodCounter;
        }
        if(this.modelCounterType == ModelCounterType.APPROXIMATE){
            return approximateCounter;
        }
        else return modelCacheCounter;
    }
}
