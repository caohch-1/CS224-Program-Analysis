package indi.ConstantPropagation;

import soot.Local;
import soot.Value;
import soot.jimple.*;

import java.util.*;

public class ConstantPropagationMap {
    Map<Local, ConstantPropagationValue> local2CPValue;

    public ConstantPropagationMap(Map<Local, ConstantPropagationValue> map) {
        local2CPValue = map;
    }

    public static ConstantPropagationMap meet(ConstantPropagationMap map1, ConstantPropagationMap map2) {
        ConstantPropagationMap resMap = new ConstantPropagationMap(new HashMap<>());

        Set<Local> localSet = new HashSet<>();
        localSet.addAll(map1.local2CPValue.keySet());
        localSet.addAll(map2.local2CPValue.keySet());
        for (Local local : localSet) {
            ConstantPropagationValue cp1 = map1.getCPValue(local);
            ConstantPropagationValue cp2 = map2.getCPValue(local);
            ConstantPropagationValue resCP = ConstantPropagationValue.meet(cp1, cp2);
            resMap.local2CPValue.put(local, resCP);
        }

        return resMap;
    }

    public ConstantPropagationValue getCPValue(Local local) {
        return local2CPValue.getOrDefault(local, UtilCPValue.UNDEF);
    }

    public ConstantPropagationValue calculate(Value value) {
        if (value instanceof Local) return getCPValue((Local) value);
        else if (value instanceof IntConstant)
            return ConstantPropagationValue.newConstantPropagationValue(((IntConstant) value).value);
        else if (value instanceof BinopExpr) {
            BinopExpr binopExpr = (BinopExpr) value;

            Value left = binopExpr.getOp1();
            ConstantPropagationValue leftCPValue = calculate(left);

            Value right = binopExpr.getOp2();
            ConstantPropagationValue rightCPValue = calculate(right);

//            System.out.printf("%s, %s\n", leftCPValue, rightCPValue);

            if (leftCPValue == UtilCPValue.UNDEF && rightCPValue == UtilCPValue.UNDEF) return UtilCPValue.UNDEF;

            if (leftCPValue == UtilCPValue.UNDEF || rightCPValue == UtilCPValue.UNDEF) return UtilCPValue.NAC;

            if (leftCPValue == UtilCPValue.NAC || rightCPValue == UtilCPValue.NAC) return UtilCPValue.NAC;

            if (binopExpr instanceof AddExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value + rightCPValue.value);
            else if (binopExpr instanceof SubExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value - rightCPValue.value);
            else if (binopExpr instanceof MulExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value * rightCPValue.value);
            else if (binopExpr instanceof DivExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value / rightCPValue.value);
            else if (binopExpr instanceof EqExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value == rightCPValue.value);
            else if (binopExpr instanceof NeExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value != rightCPValue.value);
            else if (binopExpr instanceof GtExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value > rightCPValue.value);
            else if (binopExpr instanceof LtExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value < rightCPValue.value);
            else if (binopExpr instanceof GeExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value >= rightCPValue.value);
            else if (binopExpr instanceof LeExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value <= rightCPValue.value);
            else if (binopExpr instanceof RemExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value % rightCPValue.value);
            else if (binopExpr instanceof ShlExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value << rightCPValue.value);
            else if (binopExpr instanceof ShrExpr)
                return ConstantPropagationValue.newConstantPropagationValue(leftCPValue.value >> rightCPValue.value);

        }
        return UtilCPValue.NAC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantPropagationMap that = (ConstantPropagationMap) o;
        return Objects.equals(local2CPValue, that.local2CPValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local2CPValue);
    }

    @Override
    public String toString() {
        return local2CPValue.toString();
    }
}
