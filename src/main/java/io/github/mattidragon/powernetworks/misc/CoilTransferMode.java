package io.github.mattidragon.powernetworks.misc;

public enum CoilTransferMode {
    DEFAULT,
    INPUT,
    OUTPUT;

    public static CoilTransferMode getSafe(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length)
            return DEFAULT;
        return values()[ordinal];
    }
}
