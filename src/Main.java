import predictors.GAp.GAp;
import predictors.Predictor;
import utils.Bit;
import utils.BranchResult;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Predictor gap = new GAp(2, 2, 2);


        for (int i = 0; i < 1000; i++) {
            Bit[] PC = getRandomPC(4);
            BranchResult br = getRandomBR();
            System.out.println("PC value is: " + Bit.bitArrayToString(PC) + " Branch result is: " + br);
            BranchResult r = gap.predictAndUpdate(PC, br);

        }


    }

    private static Bit[] getRandomPC(int PCSize) {
        Bit[] rPC = new Bit[PCSize];
        for (int i = 0; i < PCSize; i++) {
            Bit b = Math.random() > 0.5 ? Bit.ONE : Bit.ZERO;
            rPC[i] = b;
        }
        return rPC;
    }

    private static BranchResult getRandomBR() {
        return Math.random() < 0.75 ? BranchResult.TAKEN : BranchResult.NOT_TAKEN;
    }
}