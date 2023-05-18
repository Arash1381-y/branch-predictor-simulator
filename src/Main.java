import predictors.GAg.GAg;
import predictors.Predictor;
import utils.Bit;
import utils.BranchResult;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Predictor gag = new GAg(4, 2);


        for (int i = 0; i < 7; i++) {
            BranchResult r = gag.predictAndUpdate(new Bit[]{}, BranchResult.TAKEN);
            System.out.println(r);
        }

        System.out.println(gag.monitor());


    }
}