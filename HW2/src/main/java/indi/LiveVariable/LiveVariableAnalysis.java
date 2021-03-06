package indi.LiveVariable;

import indi.Util.IterableSolverBackward;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

public class LiveVariableAnalysis extends IterableSolverBackward {

    public LiveVariableAnalysis(UnitGraph graph) {
        super(graph);
        doAnalysis();
    }

    protected void flowThrough(FlowSet<Local> src, Unit ut, FlowSet<Local> dest) {
        src.copy(dest);

        // Remove kills
        for (ValueBox box : ut.getDefBoxes()) {
            Value value = box.getValue();
            if (value instanceof Local)
                dest.remove((Local) value);
        }

        // Add gens
        for (ValueBox box : ut.getUseBoxes()) {
            Value value = box.getValue();
            if (value instanceof Local)
                dest.add((Local) value);
        }
    }

    protected FlowSet<Local> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    protected FlowSet<Local> entryInitialFlow() {
        return new ArraySparseSet<>();

    }

    protected void merge(FlowSet<Local> dest, FlowSet<Local> src1, FlowSet<Local> src2) {
        src1.union(src2, dest);
    }

    protected void copy(FlowSet<Local> dest, FlowSet<Local> src) {
        src.copy(dest);
    }
}