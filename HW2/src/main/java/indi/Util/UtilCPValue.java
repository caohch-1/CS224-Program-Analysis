package indi.Util;

import indi.ConstantPropagation.ConstantPropagationValue;

public class UtilCPValue {
    public final static ConstantPropagationValue NAC = new ConstantPropagationValue(Integer.MIN_VALUE) {
        @Override
        public int hashCode() {
            return Integer.MIN_VALUE;
        }

        @Override
        public String toString() {
            return "NAC";
        }
    };

    public final static ConstantPropagationValue UNDEF = new ConstantPropagationValue(Integer.MAX_VALUE) {
        @Override
        public int hashCode() {
            return Integer.MAX_VALUE;
        }

        @Override
        public String toString() {
            return "UNDEF";
        }
    };
}
