package devices;

/*
Simple Register model with insert, clear and parallel read methods
 */

import utils.Bit;
import utils.Monitor;

public interface Register extends Monitor {

    public Bit[] read();

    public void load(Bit[] bits);

    public void insertBit(Bit bit);

    public void clear();
}
