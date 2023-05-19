package predictors.GAp;

import devices.*;
import predictors.Predictor;
import utils.Bit;
import utils.BranchResult;

import java.util.Arrays;

public class GAp implements Predictor {
    private final int PCMSize;
    private final Register SC; // saturating counter
    private final Register BHR; // branch history register
    private final Cache<Bit[], Bit[]> PAPHT;

    public GAp(int BHRSize, int SCSize, int PCMSize) {
        this.PCMSize = PCMSize;

        // Initialize the BHR register with the given size and null input
        this.BHR = new SerialInParallelOutRegister("bhr", BHRSize, null);

        // Initializing the PAPHT with PCMSize selector for PHT and 2^BHRSize row with SCSize as block size
        PAPHT = new PerAddressPageHistoryTable(PCMSize, (int) Math.pow(2, BHRSize), SCSize);

        // Initialize the saturating counter
        SC = new SaturatingCounter(SCSize, null);
    }


    @Override
    public BranchResult predict(Bit[] PC) {
        // get PAPHT entry by concatenating the PC and BHR
        Bit[] cacheEntry = getCacheEntry(PC);

        // Get the associated block with the cacheEntry from the PAPHT
        Bit[] cacheBlock = PAPHT.setDefault(cacheEntry, getDefaultBlock());

        // load the block into the counter
        SC.load(cacheBlock);

        // Return the predicted outcome of the branch instruction based on the value of the MSB
        return cacheBlock[0].getValue() ? BranchResult.TAKEN : BranchResult.NOT_TAKEN;
    }

    @Override
    public void update(Bit[] PC, BranchResult actual) {
        // check the predication result
        boolean isTaken = actual == BranchResult.TAKEN;

        // update saturating counter
        SC.insertBit(isTaken ? Bit.ONE : Bit.ZERO);

        // update the PAPHT
        PAPHT.put(getCacheEntry(PC), SC.read());

        // update global history
        BHR.insertBit(isTaken ? Bit.ONE : Bit.ZERO);
    }

    @Override
    public BranchResult predictAndUpdate(Bit[] PC, BranchResult actual) {
        BranchResult br = predict(PC);
        System.out.println("The predication is : " + br);
        System.out.println("Before Update: \n" + monitor());
        update(PC, actual);
        System.out.println("After Update: \n" + monitor());

        return br;
    }


    private Bit[] getCacheEntry(Bit[] PC) {
        // Get the PCMSize the least significant bits of the PC
        Bit[] pcmBits = Arrays.copyOfRange(PC, 0, PCMSize);

        // Concatenate the PCM bits with the BHR bits
        Bit[] bhrBits = BHR.read();
        Bit[] cacheEntry = new Bit[pcmBits.length + bhrBits.length];
        System.arraycopy(pcmBits, 0, cacheEntry, 0, pcmBits.length);
        System.arraycopy(bhrBits, 0, cacheEntry, pcmBits.length, bhrBits.length);

        return cacheEntry;
    }

    private Bit[] getDefaultBlock() {
        Bit[] defaultBlock = new Bit[SC.len()];
        Arrays.fill(defaultBlock, Bit.ZERO);
        return defaultBlock;
    }


    @Override
    public String monitor() {
        return "GAp predictor snapshot: \n" + BHR.monitor() + SC.monitor() + PAPHT.monitor();
    }
}
