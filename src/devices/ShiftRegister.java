package devices;


import utils.Bit;
import utils.Monitorable;

public interface ShiftRegister extends Monitorable {
    Bit[] read();

    void load(Bit[] bits);

    void insert(Bit bit);

    int getLength();

    void clear();
}
