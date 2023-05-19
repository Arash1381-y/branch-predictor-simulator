package predictors.GAs;

import devices.*;
import predictors.Predictor;
import utils.Bit;
import utils.BranchResult;

import java.util.Arrays;

public class GAs implements Predictor {

    private final int PCMSize;
    private final int KSize;
    private final Register SC; // saturating counter
    private final Register BHR; // branch history register
    private final Cache<Bit[], Bit[]> PSPHT; // Per Set Predication History Table


    public GAs(int BHRSize, int SCSize, int PCMSize, int KSize) {
        this.PCMSize = PCMSize;
        this.KSize = KSize;

        // Initialize the BHR register with the given size and null input
        this.BHR = new SerialInParallelOutRegister("bhr", BHRSize, null);

        // Initializing the PAPHT with PCMSize selector for PHT and 2^BHRSize row with SCSize as block size
        PSPHT = new PerAddressPageHistoryTable(KSize, (int) Math.pow(2, BHRSize), SCSize);

        // Initialize the saturating counter
        SC = new SaturatingCounter(SCSize, null);

    }

    /**
     * predicts the result of a branch instruction based on the global branch history and hash value of PC
     *
     * @param PC the program counter
     * @return the predicted outcome of the branch instruction (taken or not taken)
     */
    @Override
    public BranchResult predict(Bit[] PC) {
        // get PAPHT entry by concatenating the PC and BHR
        Bit[] cacheEntry = getCacheEntry(PC);

        // Get the associated block with the cacheEntry from the PSPHT
        Bit[] cacheBlock = PSPHT.setDefault(cacheEntry, getDefaultBlock());

        // load the block into the counter
        SC.load(cacheBlock);

        // Return the predicted outcome of the branch instruction based on the value of the MSB
        return cacheBlock[0].getValue() ? BranchResult.TAKEN : BranchResult.NOT_TAKEN;
    }

    /**
     * Updates the value in the cache based on actual branch result
     *
     * @param branchAddress the address of the branch
     * @param actual        the actual result of branch (Taken or Not)
     */
    @Override
    public void update(Bit[] branchAddress, BranchResult actual) {
        // check the predication result
        boolean isTaken = actual == BranchResult.TAKEN;

        // update saturating counter
        SC.insertBit(isTaken ? Bit.ONE : Bit.ZERO);

        // update the PSPHT
        PSPHT.put(getCacheEntry(branchAddress), SC.read());

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

    public String monitor() {
        return "GAp predictor snapshot: \n" + BHR.monitor() + SC.monitor() + PSPHT.monitor();
    }


    private Bit[] getCacheEntry(Bit[] PC) {
        // Get the PCMSize the least significant bits of the PC
        Bit[] pcmBits = Arrays.copyOfRange(PC, 0, PCMSize);
        Bit[] hashKSize = hash(pcmBits);


        // Concatenate the Hash PCM bits with the BHR bits
        Bit[] bhrBits = BHR.read();
        Bit[] cacheEntry = new Bit[hashKSize.length + bhrBits.length];
        System.arraycopy(hashKSize, 0, cacheEntry, 0, hashKSize.length);
        System.arraycopy(bhrBits, 0, cacheEntry, hashKSize.length, bhrBits.length);

        return cacheEntry;
    }

    private Bit[] hash(Bit[] PC) {
        Bit[] hash = new Bit[KSize];

        // XOR the first M bits of the PC to produce the hash
        for (int i = 0; i < PCMSize; i++) {
            int j = i % KSize;
            if (hash[j] == null) {
                hash[j] = PC[i];
            } else {
                Bit xorProduce = hash[j].getValue() ^ PC[i].getValue() ? Bit.ONE : Bit.ZERO;
                hash[j] = xorProduce;

            }
        }
        return hash;
    }

    private Bit[] getDefaultBlock() {
        Bit[] defaultBlock = new Bit[SC.len()];
        Arrays.fill(defaultBlock, Bit.ZERO);
        return defaultBlock;
    }
}
