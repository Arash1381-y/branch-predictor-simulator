package predictors;


import utils.Bit;
import utils.BranchPredicationResult;

public interface Predictor {

    BranchPredicationResult predict(Bit[] PC);

    void update(BranchPredicationResult actual);

}

