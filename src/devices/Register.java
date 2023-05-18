package devices;

/*
Simple Register model with insert, clear and parallel read methods
 */

import utils.Bit;
import utils.Monitor;

public interface Register extends Monitor {

    Bit[] read();

    void load(Bit[] bits);

    void insertBit(Bit bit);

    void clear();
}
