package predictors.SAs;

import devices.*;
import predictors.BranchInstruction;
import predictors.BranchPredictor;
import utils.Bit;
import utils.BranchResult;
import utils.CountMode;

import java.util.Arrays;

public class SAs implements BranchPredictor {

    private final int branchInstructionSize;
    private final int KSize;
    private final ShiftRegister SC;
    private final RegisterBank PSBHR; // per set branch history register
    private final Cache<Bit[], Bit[]> PSPHT; // per set predication history table

    public SAs(int BHRSize, int SCSize, int branchInstructionSize, int KSize) {
        this.branchInstructionSize = branchInstructionSize;
        this.KSize = KSize;

        // Initialize the PSBHR with the given bhr and branch instruction size
        PSBHR = new RegisterBank(KSize, BHRSize);

        // Initializing the PAPHT with BranchInstructionSize as PHT Selector and 2^BHRSize row as each PHT entries
        // number and SCSize as block size
        PSPHT = new PerAddressPageHistoryTable(KSize, (int) Math.pow(2, BHRSize), SCSize);

        // Initialize the SC register
        SC = new SIPORegister("sc", SCSize, null);
    }

    @Override
    public BranchResult predict(BranchInstruction branchInstruction) {
        // instruction address
        Bit[] instructionAddress = branchInstruction.getInstructionAddress();

        // get RB selector
        Bit[] selector = getAddressLine(instructionAddress);

        // select the BHR based on the selector
        ShiftRegister correspondingBHR = PSBHR.read(selector);

        // get PAPHT entry by concatenating the branch address and BHR
        Bit[] cacheEntry = getCacheEntry(selector, correspondingBHR.read());

        // get the associated block
        Bit[] cacheBlock = PSPHT.setDefault(cacheEntry, getDefaultBlock());

        // load the block into the register
        SC.load(cacheBlock);

        // Return the predicted outcome of the branch instruction based on the value of the MSB
        return cacheBlock[0].getValue() ? BranchResult.TAKEN : BranchResult.NOT_TAKEN;
    }

    @Override
    public void update(BranchInstruction branchInstruction, BranchResult actual) {
        // check the predication result
        boolean isTaken = actual == BranchResult.TAKEN;

        // update saturating counter
        Bit[] nValue = CombinationalLogic.count(SC.read(), isTaken, CountMode.SATURATING);

        // instruction address
        Bit[] instructionAddress = branchInstruction.getInstructionAddress();

        // get RB selector
        Bit[] selector = getAddressLine(instructionAddress);

        // get register from register bank
        ShiftRegister correspondingBHR = PSBHR.read(selector);

        // update the PAPHT
        PSPHT.put(getCacheEntry(selector, correspondingBHR.read()), nValue);

        // update branch history
        correspondingBHR.insert(isTaken ? Bit.ONE : Bit.ZERO);
        PSBHR.write(selector, correspondingBHR.read());
    }


    private Bit[] getAddressLine(Bit[] branchAddress) {
        // hash the branch address
        return hash(branchAddress);
    }

    private Bit[] getCacheEntry(Bit[] branchAddress, Bit[] BHRValue) {
        // Concatenate the branch address bits with the BHR bits
        Bit[] cacheEntry = new Bit[branchAddress.length + BHRValue.length];
        System.arraycopy(branchAddress, 0, cacheEntry, 0, branchInstructionSize);
        System.arraycopy(BHRValue, 0, cacheEntry, branchAddress.length, BHRValue.length);
        return cacheEntry;
    }


    /**
     * hash N bits to a K bit value
     *
     * @param bits program counter
     * @return hash value of fist M bits of `bits` in K bits
     */
    private Bit[] hash(Bit[] bits) {
        Bit[] hash = new Bit[KSize];

        // XOR the first M bits of the PC to produce the hash
        for (int i = 0; i < branchInstructionSize; i++) {
            int j = i % KSize;
            if (hash[j] == null) {
                hash[j] = bits[i];
            } else {
                Bit xorProduce = hash[j].getValue() ^ bits[i].getValue() ? Bit.ONE : Bit.ZERO;
                hash[j] = xorProduce;

            }
        }
        return hash;
    }

    /**
     * @return a zero series of bits as default value of cache block
     */
    private Bit[] getDefaultBlock() {
        Bit[] defaultBlock = new Bit[SC.getLength()];
        Arrays.fill(defaultBlock, Bit.ZERO);
        return defaultBlock;
    }

    @Override
    public String monitor() {
        return null;
    }
}
