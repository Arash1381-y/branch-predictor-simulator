package predictors;


import utils.Bit;
import utils.BranchResult;
import utils.Monitor;

public interface Predictor extends Monitor {

    /**
     * Predict if the branch is taken or not
     *
     * @param PC the program counter
     * @return predicted result of branch
     */
    BranchResult predict(Bit[] PC);

    /**
     * The dynamic predictor will update its states based on the new data
     *
     * @param actual the actual result of branch (Taken or Not)
     */
    void update(BranchResult actual);

    /**
     * despite the other methods, this function shows the passage of time. i.e. the result of branch is
     * not available at the time of predication. just a method to make a job a bit simpler
     *
     * @param PC     the program counter
     * @param actual the result of branch
     * @return the predication which is done by the dynamic predictor
     */
    BranchResult predictAndUpdate(Bit[] PC, BranchResult actual);


}

