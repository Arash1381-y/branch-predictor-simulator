package predictors.GAs;


import devices.*;
import predictors.BranchInstruction;
import predictors.BranchPredictor;
import utils.Bit;
import utils.BranchResult;
import utils.CountMode;

import java.util.Arrays;

public class GAs implements BranchPredictor {

    private final int PCMSize;
    private final int KSize;
    private final ShiftRegister SC; // saturating counter register
    private final ShiftRegister BHR; // branch history register
    private final Cache<Bit[], Bit[]> PSPHT; // Per Set Predication History Table


    /**
     * Creates a new GAs predictor with the given BHR register size and initializes the PAPHT based on
     * the Ksize and saturating counter size
     *
     * @param BHRSize               the size of the BHR register
     * @param SCSize                the size of the register which hold the saturating counter value
     * @param branchInstructionSize the number of bits which is used for saving a branch instruction
     */
    public GAs(int BHRSize, int SCSize, int branchInstructionSize, int KSize) {
        this.PCMSize = branchInstructionSize;
        this.KSize = KSize;

        // Initialize the BHR register with the given size and no default value
        BHR = new SIPORegister("bhr", BHRSize, null);

        // Initializing the PAPHT with K bit as PHT selector and 2^BHRSize row as each PHT entries
        // number and SCSize as block size
        PSPHT = new PerAddressPageHistoryTable(KSize, (int) Math.pow(2, BHRSize), SCSize);

        // Initialize the saturating counter
        SC = new SIPORegister("sc", SCSize, null);
    }

    /**
     * predicts the result of a branch instruction based on the global branch history and hash value of PC
     *
     * @param branchInstruction the branch instruction
     * @return the predicted outcome of the branch instruction (taken or not taken)
     */
    @Override
    public BranchResult predict(BranchInstruction branchInstruction) {
        // get branch address
        Bit[] branchAddress = branchInstruction.getInstructionAddress();

        // get PAPHT entry by concatenating the Branch address and BHR
        Bit[] cacheEntry = getCacheEntry(branchAddress);

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
     * @param branchInstruction the branch instruction
     * @param actual            the actual result of branch (Taken or Not)
     */
    @Override
    public void update(BranchInstruction branchInstruction, BranchResult actual) {
        // get branch address
        Bit[] branchAddress = branchInstruction.getInstructionAddress();

        // check the predication result
        boolean isTaken = actual == BranchResult.TAKEN;

        // update saturating counter
        Bit[] nValue = CombinationalLogic.count(SC.read(), isTaken, CountMode.SATURATING);
        // update the PSPHT
        PSPHT.put(getCacheEntry(branchAddress), nValue);

        // update global history
        BHR.insert(isTaken ? Bit.ONE : Bit.ZERO);
    }

    public BranchResult predictAndUpdate(BranchInstruction branchInstruction, BranchResult actual, boolean debug) {
        BranchResult br = predict(branchInstruction);
        if (debug) {
            System.out.println("The predication is : " + br);
            System.out.println("Before Update: \n" + monitor());
        }
        update(branchInstruction, actual);
        if (debug)
            System.out.println("After Update: \n" + monitor());

        return br;
    }

    /**
     * @return snapshot of caches and registers content
     */
    public String monitor() {
        return "GAp predictor snapshot: \n" + BHR.monitor() + SC.monitor() + PSPHT.monitor();
    }


    /**
     * concat the PC and BHR to retrieve the desired address
     *
     * @param PC program counter
     * @return concatenated value of first M bits of PC and BHR
     */
    private Bit[] getCacheEntry(Bit[] PC) {
        // Get the PCMSize the least significant bits of the PC
        Bit[] pcmBits = Arrays.copyOfRange(PC, 0, PCMSize);
        // get the hash value of PC in K bit
        Bit[] hashKSize = hash(pcmBits);


        // Concatenate the Hash PCM bits with the BHR bits
        Bit[] bhrBits = BHR.read();
        Bit[] cacheEntry = new Bit[hashKSize.length + bhrBits.length];
        System.arraycopy(hashKSize, 0, cacheEntry, 0, hashKSize.length);
        System.arraycopy(bhrBits, 0, cacheEntry, hashKSize.length, bhrBits.length);

        return cacheEntry;
    }

    /**
     * hash PC to a K bit value
     *
     * @param PC program counter
     * @return hash value of fist M bits of PC in K bits
     */
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
        Bit[] defaultBlock = new Bit[SC.getLength()];
        Arrays.fill(defaultBlock, Bit.ZERO);
        return defaultBlock;
    }


    public static void main(String[] args) {
        GAs gas = new GAs(4, 2, 8, 4);

        Bit[] opcode;
        Bit[] instructionAddress;
        Bit[] jumpAddress;

        for (int i = 0; i < 250; i++) {


            opcode = getRandomBitSerial(6);
            instructionAddress = getRandomBitSerial(8);
            jumpAddress = getRandomBitSerial(16);

            BranchInstruction bi = new BranchInstruction(
                    opcode,
                    instructionAddress,
                    jumpAddress
            );

            BranchResult br = getRandomBR();
            //System.out.println("PC value is: " + Bit.arrayToString(bi.getInstructionAddress()) + " Branch result is: " + br);
            BranchResult r = gas.predictAndUpdate(bi, br, false);
        }

        System.out.println(gas.monitor());

    }

    private static Bit[] getRandomBitSerial(int size) {
        Bit[] rPC = new Bit[size];
        for (int i = 0; i < size; i++) {
            Bit b = Math.random() > 0.5 ? Bit.ONE : Bit.ZERO;
            rPC[i] = b;
        }
        return rPC;
    }

    private static BranchResult getRandomBR() {
        return Math.random() < 2 ? BranchResult.TAKEN : BranchResult.NOT_TAKEN;
    }
}
