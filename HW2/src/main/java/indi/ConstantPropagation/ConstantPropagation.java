package indi.ConstantPropagation;

import indi.Util.WorkListLoopForward;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;

import java.util.HashMap;

public class ConstantPropagation extends WorkListLoopForward {

    public ConstantPropagation(DirectedGraph<Unit> graph) {
        super(graph);
        doAnalysis();
    }

    protected void flowThrough(ConstantPropagationFlow in, Unit d, ConstantPropagationFlow out) {
        copy(in, out);
        if (d instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) d;
            Value leftVal = assignStmt.getLeftOp();
            if (leftVal instanceof Local) {
                Local local = (Local) leftVal;
                Value rightVal = assignStmt.getRightOp();
                ConstantPropagationValue rightCPVal = in.calculate(rightVal);

                out.local2CPValue.put(local, rightCPVal);
            }
        }
    }

    protected ConstantPropagationFlow newInitialFlow() {
        return new ConstantPropagationFlow(new HashMap<>());
    }

    protected void merge(ConstantPropagationFlow in1, ConstantPropagationFlow in2, ConstantPropagationFlow out) {
        ConstantPropagationFlow meet = ConstantPropagationFlow.meet(in1, in2);
        copy(meet, out);
    }

    protected void copy(ConstantPropagationFlow source, ConstantPropagationFlow dest) {
        dest.local2CPValue.putAll(source.local2CPValue);
    }
}
