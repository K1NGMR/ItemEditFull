package com.itemedit.full.utils;

import org.bukkit.util.Vector;

public final class VectorUtils {
    private VectorUtils() {}

    /**
     * Normalizes a vector without producing NaN components. {@link Vector#normalize()}
     * divides by the vector length, so a zero-length vector (e.g. an entity standing
     * exactly at an explosion/knockback centre, or a look direction flattened to the
     * horizontal plane while looking straight up/down) yields (NaN, NaN, NaN). Feeding
     * that into {@code setVelocity} throws IllegalArgumentException. This returns a zero
     * vector in that degenerate case instead, so downstream {@code multiply}/{@code setY}
     * chains stay finite.
     */
    public static Vector safeNormalize(Vector v) {
        if (v == null) {
            return new Vector(0, 0, 0);
        }
        double length = v.length();
        if (length < 1.0e-6 || Double.isNaN(length)) {
            return new Vector(0, 0, 0);
        }
        return v.multiply(1.0 / length);
    }
}
