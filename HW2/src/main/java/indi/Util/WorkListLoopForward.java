package indi.Util;

import indi.ConstantPropagation.ConstantPropagationFlow;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class WorkListLoopForward extends ForwardFlowAnalysis<Unit, ConstantPropagationFlow> {
    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public WorkListLoopForward(DirectedGraph<Unit> graph) {
        super(graph);
    }

    /**
     * Given the merge of the <code>out</code> sets, compute the <code>in</code> set for <code>s</code> (or in to out,
     * depending on direction).
     * <p>
     * This function often causes confusion, because the same interface is used for both forward and backward flow analyses.
     * The first parameter is always the argument to the flow function (i.e. it is the "in" set in a forward analysis and the
     * "out" set in a backward analysis), and the third parameter is always the result of the flow function (i.e. it is the
     * "out" set in a forward analysis and the "in" set in a backward analysis).
     *
     * @param in  the input flow
     * @param d   the current node
     * @param out
     **/
    @Override
    protected void flowThrough(ConstantPropagationFlow in, Unit d, ConstantPropagationFlow out) {

    }

    /**
     * Returns the flow object corresponding to the initial values for each graph node.
     */
    @Override
    protected ConstantPropagationFlow newInitialFlow() {
        return null;
    }

    /**
     * Compute the merge of the <code>in1</code> and <code>in2</code> sets, putting the result into <code>out</code>. The
     * behavior of this function depends on the implementation ( it may be necessary to check whether <code>in1</code> and
     * <code>in2</code> are equal or aliased ). Used by the doAnalysis method.
     *
     * @param in1
     * @param in2
     * @param out
     */
    @Override
    protected void merge(ConstantPropagationFlow in1, ConstantPropagationFlow in2, ConstantPropagationFlow out) {

    }

    /**
     * Creates a copy of the <code>source</code> flow object in <code>dest</code>.
     *
     * @param source
     * @param dest
     */
    @Override
    protected void copy(ConstantPropagationFlow source, ConstantPropagationFlow dest) {

    }
}
