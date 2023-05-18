package predictors;


import utils.Bit;
import utils.BranchPredicationResult;

public interface Predictor {

    public BranchPredicationResult predict(Bit[] PC);

}

