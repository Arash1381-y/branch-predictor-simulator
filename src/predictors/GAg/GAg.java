package predictors.GAg;

import devices.*;
import predictors.Predictor;
import utils.Bit;
import utils.BranchResult;

import java.util.Arrays;

public class GAg implements Predictor {

    private final Register BHR; // branch history register
    private final Register SC; // saturating counter
    private final Cache<Bit[], Bit[]> PHT; // page history table

    /**
     * Creates a new GAg predictor with the given BHR register size and initializes the BHR and PHT.
     *
     * @param BHRSize the size of the BHR register
     * @param SCSize  the size of the saturating counter
     */
    public GAg(int BHRSize, int SCSize) {
        // Initialize the BHR register with the given size and null input
        this.BHR = new SerialInParallelOutRegister("bhr", BHRSize, null);

        // Initialize the PHT with a size of 2^size and each entry having a saturating counter of size "size"
        PHT = new PageHistoryTable((int) Math.pow(2, BHRSize), SCSize);

        // Initialize the saturating counter
        SC = new SaturatingCounter(SCSize, null);
    }

    /**
     * Predicts the outcome of a branch instruction based on the current state of the GAg predictor.
     *
     * @param PC the program counter of the branch instruction
     * @return the predicted outcome of the branch instruction (taken or not taken)
     */
    @Override
    public BranchResult predict(Bit[] PC) {
        // Read the current value of the BHR register
        Bit[] BHRValue = BHR.read();

        // Get the associated block with the current value of the BHR register from the PHT
        Bit[] cacheBlock = PHT.getOrDefault(BHRValue, getDefaultBlock());

        // load the block into the counter
        SC.load(cacheBlock);

        // Return the predicted outcome of the branch instruction based on the value of the MSB
        return cacheBlock[0].getValue() ? BranchResult.TAKEN : BranchResult.NOT_TAKEN;
    }

    /**
     * Updates the values in the cache based on the branch result
     *
     * @param actual the actual result of the branch condition
     */
    @Override
    public void update(BranchResult actual) {
        // check the predication result
        boolean isTaken = actual == BranchResult.TAKEN;

        // update saturating counter
        SC.insertBit(isTaken ? Bit.ONE : Bit.ZERO);

        // add updated value to the cache
        PHT.put(BHR.read(), SC.read());

        // update global history
        BHR.insertBit(isTaken ? Bit.ONE : Bit.ZERO);
    }

    @Override
    public BranchResult predictAndUpdate(Bit[] PC, BranchResult actual) {
        BranchResult br = predict(PC);
        update(actual);

        return br;
    }

    private Bit[] getDefaultBlock() {
        Bit[] defaultBlock = new Bit[SC.len()];
        Arrays.fill(defaultBlock, Bit.ZERO);
        return defaultBlock;
    }
}
