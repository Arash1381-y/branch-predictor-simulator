package utils;

import java.util.StringJoiner;

public enum Bit {
    ZERO(false),
    ONE(true);

    private final boolean value;

    private Bit(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return value ? "1" : "0";
    }

    public static String bitArrayToString(Bit[] bitArray) {
        StringJoiner sj = new StringJoiner("", "", "");
        for (Bit bit : bitArray) {
            sj.add(bit.toString());
        }
        return sj.toString();
    }
}
