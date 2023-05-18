package devices;

/*
Simple Register model with insert and clear methods
 */

import utils.Bit;
import utils.Monitor;

public interface Register extends Monitor {

    public Bit[] read();

    public void insertBit(Bit bit);

    public void clear();
}
