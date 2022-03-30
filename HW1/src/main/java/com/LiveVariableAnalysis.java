package com;

import soot.*;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;

import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class LiveVariableAnalysis extends BackwardFlowAnalysis {

    public LiveVariableAnalysis(UnitGraph graph) {
        super(graph);

        doAnalysis();
    }

    @Override
    protected void flowThrough(Object srcValue, Object u, Object destValue) {
        FlowSet src = (FlowSet) srcValue, dest = (FlowSet) destValue;
        Unit ut = (Unit) u;
        src.copy(dest);

        // Remove kills
        for (ValueBox box : ut.getDefBoxes())
        {
            Value value = box.getValue();
            if( value instanceof Local)
                dest.remove( value );
        }

        // Add gens
        for (ValueBox box : ut.getUseBoxes())
        {
            Value value = box.getValue();
            if (value instanceof Local)
                dest.add(value);
        }
    }

    @Override
    protected Object newInitialFlow() {
        return new ArraySparseSet();
    }

    @Override
    protected Object entryInitialFlow() {
        return new ArraySparseSet();

    }

    @Override
    protected void merge(Object src1, Object src2, Object dest) {
        FlowSet sourceSet1 = (FlowSet) src1, sourceSet2 = (FlowSet) src2, destSet = (FlowSet) dest;
        sourceSet1.union(sourceSet2, destSet);

    }

    @Override
    protected void copy(Object src, Object dest) {
        FlowSet sourceSet = (FlowSet) src, destSet = (FlowSet) dest;
        sourceSet.copy(destSet);
    }

    public static void setupSoot() {
        // Soot class path
        String classesDir = "/Users/caohch1/Projects/ProgramAnalysis/HW1/src/main/java";
        String jceDir = System.getProperty("java.home") + "/lib/jce.jar";
        String jrtDir = System.getProperty("java.home") + "/lib/rt.jar";
        String path = jrtDir + File.pathSeparator + jceDir + File.pathSeparator + classesDir;
        Scene.v().setSootClassPath(path);
        Options.v().set_process_dir(Arrays.asList(classesDir));
        Options.v().set_whole_program(true);

        // Main class
        String className = "com.Hello";
        SootClass mainClass = Scene.v().loadClassAndSupport(className);
        Scene.v().setMainClass(mainClass);
    }

    public static void main(String[] args) {
        setupSoot();

        SootClass mainClass = Scene.v().getMainClass();
        System.out.println(mainClass.getMethods());

    }
}
