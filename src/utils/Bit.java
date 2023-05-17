package utils;

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
}
