package indi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Unit;
import soot.jimple.*;


public enum MethodHelper {
    INTERFACE("invokeinterface"), VIRTUAL("invokevirtual"), SPECIAL("invokespecial"), STATIC("invokestatic");

    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");
    String methodKind;

    MethodHelper(String kind) {
        methodKind = kind;
    }

    public static MethodHelper getMethodKind(Unit unit) throws IllegalArgumentException {
        InvokeExpr invokeExpr = ((Stmt) unit).getInvokeExpr();
        if (invokeExpr instanceof InterfaceInvokeExpr) return INTERFACE;
        else if (invokeExpr instanceof VirtualInvokeExpr) return VIRTUAL;
        else if (invokeExpr instanceof SpecialInvokeExpr) return SPECIAL;
        else if (invokeExpr instanceof StaticInvokeExpr) return STATIC;
        else {
            logger.error(String.format("%s has illegal call.", invokeExpr.toString()));
        }
        return null;
    }
}
