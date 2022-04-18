package indi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;
import soot.options.Options;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;

import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

public class LiveVariableAnalysisSoot extends BackwardFlowAnalysis<Unit, FlowSet<Object>> {

    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");

    public LiveVariableAnalysisSoot(UnitGraph graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Object> src, Unit ut, FlowSet<Object> dest) {
        src.copy(dest);

        // Remove kills
        for (ValueBox box : ut.getDefBoxes()) {
            Value value = box.getValue();
            if (value instanceof Local)
                dest.remove(value);
        }

        // Add gens
        for (ValueBox box : ut.getUseBoxes()) {
            Value value = box.getValue();
            if (value instanceof Local)
                dest.add(value);
        }
    }

    @Override
    protected FlowSet<Object> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<Object> entryInitialFlow() {
        return new ArraySparseSet<>();

    }

    @Override
    protected void merge(FlowSet<Object> dest, FlowSet<Object> src1, FlowSet<Object> src2) {
        src1.union(src2, dest);
    }

    @Override
    protected void copy(FlowSet<Object> dest, FlowSet<Object> src) {
        src.copy(dest);
    }

    public static void setupSoot(String className) {
        // Soot class path
        String classesDirMain = "./src/main/java";
        String classesDirTest = "./src/test/java";
        String classesDirCurr = "./";
        String jceDir = System.getProperty("java.home") + "/lib/jce.jar";
        String jrtDir = System.getProperty("java.home") + "/lib/rt.jar";
        String path = jrtDir + File.pathSeparator + jceDir;
        path += File.pathSeparator + classesDirMain + File.pathSeparator + classesDirTest + File.pathSeparator + classesDirCurr;

        // Init Scene
        Scene.v().setSootClassPath(path);

        // Add necessary opts
        Options.v().set_process_dir(Collections.singletonList(classesDirMain));
        Options.v().set_process_dir(Collections.singletonList(classesDirTest));

        // Set Main class
        SootClass mainClass = Scene.v().loadClassAndSupport(className);
        Scene.v().setMainClass(mainClass);
    }

    public static void main(String[] args) throws IOException {
        String mainClassName = "Calculate";
        try {
            setupSoot(mainClassName);
        } catch (CompilationDeathException e) {
            logger.error(e.toString());
            return;
        }

        soot.Main.main(args);
        logger.info("---------soot.Main.main()---------");

        SootClass mainClass = Scene.v().getMainClass();
        logger.info(String.format("Loading Class: %s ...", mainClass.getName()));

        String methodSignature = "void main(java.lang.String[])";
        SootMethod mainMethod = mainClass.getMethod(methodSignature);
        logger.info(String.format("Loading Method: %s ...", mainMethod));

        Body jimpleBody = mainMethod.retrieveActiveBody();
        logger.info("Retrieving method body ...");

        String rootPath = "/Users/caohch1/Projects/ProgramAnalysis/HW1/jimples/";
        String filePath = rootPath + mainClass.getName() + "-" + mainMethod.getName() + ".jimple";
        FileWriter fileWriter = new FileWriter(filePath);
        try (BufferedWriter out = new BufferedWriter(fileWriter)) {
            out.write(jimpleBody.toString());
        } finally {
            logger.info(String.format("Writing %s ...", filePath));
        }

        UnitGraph unitGraph = new ClassicCompleteUnitGraph(jimpleBody);
        logger.info(String.format("Creating unitGraph with %d units ...", unitGraph.size()));

        LiveVariableAnalysisSoot liveVariableAnalysisSoot = new LiveVariableAnalysisSoot(unitGraph);
        logger.info(String.format("Loading %s, LVA initializing ...", liveVariableAnalysisSoot.getClass()));

        UnitPatchingChain units = jimpleBody.getUnits();

        for (int unitID = 0; unitID < unitGraph.size(); unitID++) {
            Object unit = units.toArray()[unitID];
            logger.info(String.format("Analyzing units[%d]: %s ...", unitID, unit.toString()));

            Object lvBefore = liveVariableAnalysisSoot.getFlowBefore((Unit) unit);
            Object lvAfter = liveVariableAnalysisSoot.getFlowAfter((Unit) unit);
            logger.info(String.format("LV Before units[%d]: %s", unitID, lvBefore));
            logger.info(String.format("LV After  units[%d]: %s\n", unitID, lvAfter));
        }
    }
}
