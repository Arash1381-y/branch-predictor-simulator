package devices;

/*
 * our page History Tale
 * read below assumptions about pre-defined PHT
 * ------------------------------------------------------
 * ASSUMPTIONS:
 * 1) the entry of the PHT is a # of bits which # is given as PHT initial param
 *
 * 2) each entry of the PHT is mapped to a #-bit saturate counter which # is given as PHT initial param
 *
 * 3) when PHT is being read, the corresponding to saturate counter value will be return
 *
 * 4) there is no checker if the in value of PHT is bigger than the cache last entry address
 * therefore be aware! your bug won't throw any error here
 * -------------------------------------------------------
 */

import utils.Bit;

import java.util.Arrays;
import java.util.HashMap;

public class PageHistoryTable implements Cache<Bit[], Bit[]> {

    private final int nRows; // number of PHT entries
    private final int nColumns; // number of saturn counters' bits
    private final HashMap<String, Bit[]> PHT; // save entry and saturate counters in PHT.


    public PageHistoryTable(int nRows, int nColumns) {
        this.nRows = nRows;
        this.nColumns = nColumns;
        this.PHT = new HashMap<>();
    }

    @Override
    public Bit[] get(Bit[] key) {
        return PHT.getOrDefault(Arrays.toString(key), null);
    }

    @Override
    public void put(Bit[] key, Bit[] value) {
        if (value.length != nColumns)
            throw new RuntimeException("invalid number of bits for saturate counter");

        String entry = Arrays.toString(key);
        PHT.put(entry, value);
    }

    @Override
    public void clear() {
        PHT.clear();
    }
}
