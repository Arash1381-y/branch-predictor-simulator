package devices;

/*
Simple Register model with insert and clear methods
 */

import utils.Bit;

public interface Register {
    public void insertBit(Bit bit);
    public void clear();
}
