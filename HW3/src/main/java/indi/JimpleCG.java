package indi;

import soot.*;
import soot.jimple.Stmt;

import java.util.*;

public class JimpleCG {
    Collection<SootMethod> startPoints;

    Set<SootMethod> reachableMethods;

    Map<SootMethod, Set<Edge>> cer2cee;
    Map<SootMethod, Set<Edge>> cee2cer;

    Map<Unit, SootMethod> unit2Holder;

    public JimpleCG() {
        reachableMethods = new HashSet<>();
        cer2cee = new HashMap<>();
        cee2cer = new HashMap<>();
        unit2Holder = new HashMap<>();
        startPoints = new LinkedList<>();

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.getName().equals("main")) startPoints.add(sootMethod);

                if (!sootMethod.isConcrete()) continue;
                Body body = sootMethod.retrieveActiveBody();
                for (Unit unit : body.getUnits()) {
                    unit2Holder.put(unit, sootMethod);
                }
            }
        }

        reachableMethods.addAll(startPoints);
    }

    public Collection<Unit> getCallSite(SootMethod sootMethod) {
        List<Unit> callSites = new LinkedList<>();
        Body body = sootMethod.retrieveActiveBody();
        for (Unit unit : body.getUnits()) {
            Stmt stmt = (Stmt) unit;
            if (stmt.containsInvokeExpr()) {
                callSites.add(stmt);
            }
        }

        return callSites;
    }

    public void addEdge(Unit cs, SootMethod ce, MethodHelper kind) {
        reachableMethods.add(ce);

        Edge callEdge = new Edge(kind, cs, ce);

        Set<Edge> callers = cee2cer.computeIfAbsent(ce, k -> new HashSet<>());
        callers.add(callEdge);

        SootMethod caller = unit2Holder.get(cs);
        Set<Edge> callees = cer2cee.computeIfAbsent(caller, k -> new HashSet<>());
        callees.add(callEdge);
    }

    public Set<Edge> getCallOut(SootMethod sootMethod) {
        return Collections.unmodifiableSet(cer2cee.computeIfAbsent(sootMethod, k -> new HashSet<>()));
    }
}
