package indi.ConstantPropagation;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;

public class ConstantPropagation extends ForwardFlowAnalysis<Unit, ConstantPropagationMap> {

    public ConstantPropagation(DirectedGraph<Unit> graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(ConstantPropagationMap in, Unit d, ConstantPropagationMap out) {
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

    @Override
    protected ConstantPropagationMap newInitialFlow() {
        return new ConstantPropagationMap(new HashMap<>());
    }

    @Override
    protected void merge(ConstantPropagationMap in1, ConstantPropagationMap in2, ConstantPropagationMap out) {
        ConstantPropagationMap meet = ConstantPropagationMap.meet(in1, in2);
        copy(meet, out);
    }

    @Override
    protected void copy(ConstantPropagationMap source, ConstantPropagationMap dest) {
        dest.local2CPValue.putAll(source.local2CPValue);
    }
}
