package devices;

/*
 * our Register Model
 * read below assumption about pre-defined Register
 * ------------------------------------------------------
 * ASSUMPTIONS
 * 1) The data in register won't change unless one of it functions is
 * called and the function changes the register field.
 *
 * 2) The default behavior of register on insert function is to shift all
 * the values to right and add the new bit to the first element of array
 * (think of it as queue first in -> first out)
 *
 * 3) data of register can be read in parallel manner (pretty similar to serial-in parallel-out
 *  shift register)
 * -------------------------------------------------------
 */


import utils.Bit;

import java.util.Arrays;

public class SerialInParallelOutRegister implements Register {
    private final Bit[] register;
    private final int size;

    public final String name;

    /**
     * Creates a new register with the specified size and default value.
     * If default_value is null, the register is zero-filled by default.
     *
     * @param size         the size of the register
     * @param defaultValue the default value to initialize the register with
     */
    public SerialInParallelOutRegister(String name, int size, Bit[] defaultValue) {
        this.name = name;
        this.size = size;
        this.register = new Bit[size];
        if (defaultValue == null) {
            // fill all the register with zero values
            clear();
        } else {
            // fill all the register with default value
            System.arraycopy(Arrays.copyOf(defaultValue, size), 0, this.register, 0, size);
        }
    }

    @Override
    public Bit[] read() {
        return Arrays.copyOf(register, size);
    }

    /**
     * Inserts a new bit at the beginning of the register and shifts all existing bits
     * to the right. The new bit is represented using a Bit enum.
     *
     * @param bit the Bit enum representing the new bit to be inserted
     */
    public void insertBit(Bit bit) {
        // Shift all existing bits to the right by one position
        for (int i = this.register.length - 1; i > 0; i--) {
            this.register[i] = this.register[i - 1];
        }

        // Insert the new bit at the beginning of the register
        this.register[0] = bit;
    }

    /**
     * clear the register and set teh register value to 0
     */
    @Override
    public void clear() {
        for (int i = 0; i < this.size; i++) {
            this.register[i] = Bit.ZERO;
        }
    }

    /**
     * Returns the contents of the register as a binary string.
     * Each bit is represented as a 0 or 1 character.
     *
     * @return the binary string representation of the register
     */
    public String monitor() {
        StringBuilder sb = new StringBuilder();
        for (Bit bit : this.register) {
            sb.append((bit.getValue()) ? "1" : "0");
        }
        return sb.toString();
    }

    // simple test
    public static void main(String[] args) {
        Register r = new SerialInParallelOutRegister("t", 4, new Bit[]{Bit.ZERO, Bit.ONE, Bit.ZERO, Bit.ZERO});
        Bit[] data = r.read();
        System.out.println(Arrays.toString(data));
        r.insertBit(Bit.ONE);
        data = r.read();
        System.out.println(Arrays.toString(data));
    }
}



