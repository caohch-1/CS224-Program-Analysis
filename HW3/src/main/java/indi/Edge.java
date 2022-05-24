package indi;

import soot.SootMethod;
import soot.Unit;

import java.util.Objects;

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
