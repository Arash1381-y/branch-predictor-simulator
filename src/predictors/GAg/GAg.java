package predictors.GAg;

import devices.Cache;
import devices.PageHistoryTable;
import devices.Register;
import devices.SerialInParallelOutRegister;
import predictors.Predictor;
import utils.Bit;
import utils.BranchPredicationResult;

public class GAg implements Predictor {

    private final Register BHR; // branch history register
    private final Cache<Bit[], Bit[]> PHT; // page history table

    /**
     * Creates a new GAg predictor with the given BHR register size and initializes the BHR and PHT.
     *
     * @param size the size of the BHR register
     */
    public GAg(int size) {
        // Initialize the BHR register with the given size and null input
        this.BHR = new SerialInParallelOutRegister("bhr", size, null);

        // Initialize the PHT with a size of 2^size and each entry having a saturate counter of size "size"
        PHT = new PageHistoryTable((int) Math.pow(2, size), size);
    }

    /**
     * Predicts the outcome of a branch instruction based on the current state of the GAg predictor.
     *
     * @param PC the program counter of the branch instruction
     * @return the predicted outcome of the branch instruction (taken or not taken)
     */
    @Override
    public BranchPredicationResult predict(Bit[] PC) {
        // Read the current value of the BHR register
        Bit[] BHRValue = BHR.read();

        // Get the saturate counter associated with the current value of the BHR register from the PHT
        Bit[] saturateCounter = PHT.getOrDefault(BHRValue, null);

        // Return the predicted outcome of the branch instruction based on the value of the first bit of the saturate counter
        return saturateCounter[0].getValue() ? BranchPredicationResult.TAKEN : BranchPredicationResult.NOT_TAKEN;
    }
}
