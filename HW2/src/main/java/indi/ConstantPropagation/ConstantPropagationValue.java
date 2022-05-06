package indi.ConstantPropagation;

import indi.Util.UtilCPValue;

import java.util.Objects;

public class ConstantPropagationValue {
    int value;

    public ConstantPropagationValue(int value) {
        this.value = value;
    }

    public static ConstantPropagationValue newConstantPropagationValue(int value) {
        return new ConstantPropagationValue(value);
    }

    public static ConstantPropagationValue newConstantPropagationValue(boolean value) {
        return value ? newConstantPropagationValue(1) : newConstantPropagationValue(0);
    }

    public static ConstantPropagationValue newConstantPropagationValue(String value) {
        if (Objects.equals(value, "NAC")) return UtilCPValue.NAC;
        else return UtilCPValue.UNDEF;
    }

    public static ConstantPropagationValue meet(ConstantPropagationValue cpValue1, ConstantPropagationValue cpValue2) {
        ConstantPropagationValue NAC = newConstantPropagationValue("NAC");
        ConstantPropagationValue UNDEF = newConstantPropagationValue("UNDEF");

        if (cpValue1 == UNDEF) return cpValue2;

        if (cpValue2 == UNDEF) return cpValue1;

        if (cpValue1 == NAC || cpValue2 == NAC) return NAC;

        if (cpValue1.value == cpValue2.value) return newConstantPropagationValue(cpValue1.value);

        return NAC;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (this == UtilCPValue.UNDEF && o == UtilCPValue.NAC) return false;
        if (this == UtilCPValue.NAC && o == UtilCPValue.UNDEF) return false;
        ConstantPropagationValue that = (ConstantPropagationValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
