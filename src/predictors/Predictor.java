package predictors;


import utils.BranchPredicationResult;

public interface Predictor {

    public BranchPredicationResult predict(boolean[] PC);

}

