package indi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;

public class Edge {
    MethodHelper methodKind;
    Unit callSite;
    SootMethod callee;

    public Edge(MethodHelper kind, Unit cs, SootMethod cee) {
        methodKind = kind;
        callSite = cs;
        callee = cee;
    }
}

enum MethodHelper {
    VIRTUAL, STATIC, SPECIAL, INTERFACE;

    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");

    public static MethodHelper getMethodKind(Unit unit) {
        InvokeExpr invokeExpr = ((Stmt) unit).getInvokeExpr();

        if (invokeExpr instanceof StaticInvokeExpr) return STATIC;
        else if (invokeExpr instanceof VirtualInvokeExpr) return VIRTUAL;
        else if (invokeExpr instanceof SpecialInvokeExpr) return SPECIAL;
        else if (invokeExpr instanceof InterfaceInvokeExpr) return INTERFACE;
        else {
            logger.error(String.format("%s has illegal call.", invokeExpr.toString()));
        }
        return null;
    }
}
