package com.recyclix.backend.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class MoneyUtils {

    public static BigDecimal scale(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(Constants.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " ne peut pas être null");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " doit être > 0");
        }
    }

    public static void requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " ne peut pas être null");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " doit être >= 0");
        }
    }

    public static BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        BigDecimal x = a == null ? BigDecimal.ZERO : a;
        BigDecimal y = b == null ? BigDecimal.ZERO : b;
        return scale(x.add(y));
    }

    public static BigDecimal safeSubtract(BigDecimal a, BigDecimal b) {
        BigDecimal x = a == null ? BigDecimal.ZERO : a;
        BigDecimal y = b == null ? BigDecimal.ZERO : b;
        return scale(x.subtract(y));
    }

    public static boolean isGreaterThan(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.compareTo(b) > 0;
    }

    public static boolean isLessThan(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.compareTo(b) < 0;
    }

    public static boolean isEqual(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }
}