import predictors.BranchInstruction;
import predictors.BranchPredictor;
import predictors.GAp.GAp;
import predictors.GAs.GAs;
import predictors.PAg.PAg;
import predictors.PAp.PAp;
import utils.Bit;
import utils.BranchResult;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {


    public static BranchResult predictAndUpdate(BranchPredictor predictor, BranchInstruction branchInstruction, BranchResult actual, boolean debug) {
        BranchResult br = predictor.predict(branchInstruction);
        if (debug) {
            System.out.println("The predication is : " + br);
            System.out.println("Before Update: \n" + predictor.monitor());
        }
        predictor.update(branchInstruction, actual);
        if (debug)
            System.out.println("After Update: \n" + predictor.monitor());

        return br;
    }

    public static void main(String[] args) {
        PAp pAp = new PAp(4, 2, 4);

        Bit[] opcode;
        Bit[] instructionAddress;
        Bit[] jumpAddress;


        double hit = 0;
        double total = 10000;

        for (int i = 0; i < total; i++) {


            opcode = getRandomBitSerial(6);
            instructionAddress = getRandomBitSerial(4);
            jumpAddress = getRandomBitSerial(16);

            BranchInstruction bi = new BranchInstruction(
                    opcode,
                    instructionAddress,
                    jumpAddress
            );

            BranchResult br = getRandomBR();
            System.out.println("PC value is: " + Bit.arrayToString(bi.getInstructionAddress()) + " Branch result is: " + br);
            BranchResult pbr = predictAndUpdate(pAp, bi, br, false);
            if (br == pbr) {
                hit++;
            }
        }

        System.out.println(pAp.monitor());


        System.out.println("hit rate is equal to : " + (hit / total));

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
        return Math.random() < 0.6 ? BranchResult.TAKEN : BranchResult.NOT_TAKEN;
    }
}