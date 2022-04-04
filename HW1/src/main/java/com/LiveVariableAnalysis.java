package com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LiveVariableAnalysis {
    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");
    MyCFG myCFG;

    public LiveVariableAnalysis(String className, String[] args) {
        try {
            setupSoot(className);
        } catch (CompilationDeathException e) {
            logger.error(e.toString());
            return;
        }
        soot.Main.main(args);
        logger.info(String.format("Setup %s Done, Soot start", className));

        SootClass mainClass = Scene.v().getMainClass();
        String methodSignature = "void main(java.lang.String[])";
        SootMethod mainMethod = mainClass.getMethod(methodSignature);
        Body jimpleBody = mainMethod.retrieveActiveBody();
        myCFG = new MyCFG(jimpleBody);
        logger.info(String.format("Generate CFG for %s Done\n", mainMethod));

    }

    public static void setupSoot(String className) {
        // Soot class path
        String classesDirMain = "./src/main/java";
        String classesDirTest = "./src/test/java";
        String jceDir = System.getProperty("java.home") + "/lib/jce.jar";
        String jrtDir = System.getProperty("java.home") + "/lib/rt.jar";
        String path = jrtDir + File.pathSeparator + jceDir;
        path += File.pathSeparator + classesDirMain + File.pathSeparator + classesDirTest;

        // Init Scene
        Scene.v().setSootClassPath(path);

        // Add necessary opts
        Options.v().set_process_dir(Collections.singletonList(classesDirMain));
        Options.v().set_process_dir(Collections.singletonList(classesDirTest));

        // Set Main class
        SootClass mainClass = Scene.v().loadClassAndSupport(className);
        Scene.v().setMainClass(mainClass);
    }

    public static void main(String[] args) {
        String mainClassName = "Calculate";

        LiveVariableAnalysis liveVariableAnalysis = new LiveVariableAnalysis(mainClassName, args);
        liveVariableAnalysis.doAnalysis();

        // Todo: From tail use getPredsOf to travel graph
        int unitID = 1;
        for (CFGNode cfgNode : liveVariableAnalysis.getMyCFG().getCfgNodes()) {
            logger.info(String.format("[%d]: %s", unitID, cfgNode));
            liveVariableAnalysis.transferNode(cfgNode);
            logger.info(String.format("[%d]: %s\n", unitID, cfgNode));
            unitID += 1;
        }

//        String mainClassName = "Calculate";
//        String mainClassPath = String.format("./target/test-classes/%s.class", mainClassName);
//        ClassFile mainClassFile = new ClassFile(mainClassName);
//        FileInputStream is = new FileInputStream(mainClassPath);
//        mainClassFile.loadClassFile(is);
//        logger.info(String.format("Loading Class: %s ...", mainClassFile));
//
//        method_info methodInfo = null;
//        for (method_info method: mainClassFile.methods) {
//            if (Objects.equals(method.toName(mainClassFile.constant_pool), "main")) {
//                methodInfo = method;
//            }
//        }
//        logger.info(String.format("Loading method_info: %s ...", methodInfo.toName(mainClassFile.constant_pool)));
//
//        mainClassFile.parseMethod(methodInfo);
//        CFG cfg = new CFG(methodInfo);
//
//        JimpleBody jimpleBody = new JimpleBody();
//        cfg.jimplify(mainClassFile.constant_pool, mainClassFile.this_class, mainClassFile.bootstrap_methods_attribute, jimpleBody);
//
//        UnitGraph unitGraph = new ClassicCompleteUnitGraph(jimpleBody);
//        logger.info(String.format("Creating unitGraph with %d units ...", unitGraph.size()));
    }

    void newBoundaryFact() {
        for (CFGNode cfgTailNode : myCFG.getTails()) {
            cfgTailNode.setInSet(new HashSet<>());
        }
    }

    void newInitialFact() {
        for (CFGNode cfgNode : myCFG.getCfgNodes()) {
            if (!cfgNode.isTail()) {
                cfgNode.setInSet(new HashSet<>());
            }
        }
    }

    void meetInto(CFGNode upNode, CFGNode downNode) {
        upNode.getOutSet().addAll(downNode.getInSet());

    }

    void transferNode(CFGNode node) {
        Unit unit = node.getUnit();
        Set<Local> defLocals = new HashSet<>();
        Set<Local> useLocals = new HashSet<>();

        // Def, need kill
        List<ValueBox> defBoxes = unit.getDefBoxes();
        for (ValueBox vb : defBoxes) {
            Value value = vb.getValue();
            if (value instanceof Local) {
                defLocals.add((Local) value);
            }
        }

        // Use, need gen
        List<ValueBox> useBoxes = unit.getUseBoxes();
        for (ValueBox vb : useBoxes) {
            Value value = vb.getValue();
            if (value instanceof Local) {
                useLocals.add((Local) value);
            }
        }

        node.setInSet(new HashSet<>());
        node.getInSet().addAll(node.getOutSet());
        node.getInSet().removeAll(defLocals);
        node.getInSet().addAll(useLocals);
    }

    // Todo: Implement Iterator
    void doAnalysis() {
        newBoundaryFact();
        newInitialFact();
    }

    public MyCFG getMyCFG() {
        return myCFG;
    }

    public void setMyCFG(MyCFG myCFG) {
        this.myCFG = myCFG;
    }
}
