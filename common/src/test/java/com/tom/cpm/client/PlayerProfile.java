package com.tom.cpm.client;

import java.util.function.BooleanSupplier;

public final class PlayerProfile {
    private static BooleanSupplier registeredSupplier;

    private PlayerProfile() {
    }

    public static void addInFirstPerson(BooleanSupplier supplier) {
        registeredSupplier = supplier;
    }

    public static BooleanSupplier registeredSupplier() {
        return registeredSupplier;
    }

    public static void reset() {
        registeredSupplier = null;
    }
}
