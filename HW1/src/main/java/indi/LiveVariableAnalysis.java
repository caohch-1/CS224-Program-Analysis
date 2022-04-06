package indi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LiveVariableAnalysis {
    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");
    MyCFG myCFG;
    SootClass mainClass;

    String classesDirMain = "./src/main/java/";
    String classesDirTest = "./src/test/java/";

    public LiveVariableAnalysis(String className, String methodName) {
        setupSoot();

        try {
            mainClass = Scene.v().loadClassAndSupport(className);
        } catch (CompilationDeathException e) {
            logger.error(e.toString());
            System.exit(-1);
        }

        SootMethod method = mainClass.getMethodByName(methodName);
        Body jimpleBody = method.retrieveActiveBody();
        myCFG = new MyCFG(jimpleBody);
        logger.info(String.format("Soot Class: %s, Method: %s", mainClass.getName(), method.getName()));
        logger.info(String.format("Soot method locals: %s", jimpleBody.getLocals()));
    }

    public static void main(String[] args) {
        String mainClassName = "test0";
        String mainMethodName = "main";

        LiveVariableAnalysis liveVariableAnalysis = new LiveVariableAnalysis(mainClassName, mainMethodName);
        liveVariableAnalysis.doAnalysisAndShow();

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

    void setupSoot() {
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(classesDirMain));
        Options.v().set_process_dir(Collections.singletonList(classesDirTest));

        Options.v().set_keep_line_number(true);
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
        Set<Local> temp = upNode.getOutSet();
        temp.addAll(downNode.getInSet());
        upNode.setOutSet(temp);
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

        Set<Local> temp = new HashSet<>(node.getOutSet());
        temp.removeAll(defLocals);
        temp.addAll(useLocals);

        node.setInSet(temp);
    }

    int doAnalysis() {
        newBoundaryFact();
        newInitialFact();

        int iterNum = 0;
        boolean ifChange = true;
        while (ifChange) {
            iterNum += 1;
            ifChange = false;
            Stack<CFGNode> stack = new Stack<>();
            Set<CFGNode> visited = new HashSet<>();
            stack.addAll(getMyCFG().getTails());
            while (stack.size() != 0) {
                // Get predNodes for future travel
                CFGNode node = stack.pop();
                visited.add(node);
                ArrayList<CFGNode> predNodes = getMyCFG().getPredsOf(node);
                for (CFGNode predNode : predNodes) {
                    if (!visited.contains(predNode)) stack.push(predNode);
                }

                Set<Local> inSetBefore = new HashSet<>(node.getInSet());
                Set<Local> outSetBefore = new HashSet<>(node.getOutSet());

                //Calculate Meet&Transfer
                ArrayList<CFGNode> succNodes = getMyCFG().getSuccsOf(node);
                if (succNodes.size() == 0) continue;

                for (CFGNode succNode : succNodes) {
                    meetInto(node, succNode);
                }
                transferNode(node);

                if (!Objects.equals(inSetBefore.toString(), node.getInSet().toString()) ||
                        !Objects.equals(outSetBefore.toString(), node.getOutSet().toString()))
                    ifChange = true;
            }
        }
        return iterNum;
    }

    void doAnalysisAndShow() {
        String[] sourceCodes = readFile(classesDirTest + mainClass.getName() + ".java");

        int iterNum = doAnalysis();
        System.out.printf("Iteration %d times\n", iterNum);
        int stmtID = 0, sourceID = 0;
        for (CFGNode cfgNode : myCFG.getCfgNodes()) {
            stmtID += 1;
            int newSourceID = cfgNode.getUnit().getJavaSourceStartLineNumber();
            if (newSourceID != sourceID) {
                sourceID = newSourceID;
                System.out.printf("\n=====Source[%d]: %s=====\n", sourceID, sourceCodes[sourceID - 1].replaceAll(" ", ""));
            }
            System.out.printf("StateM[%d]: %s,\nOutSet[%d]: %s\n", stmtID, cfgNode.getUnit(), stmtID, cfgNode.getOutSet());
        }
    }

    String[] readFile(String filePath) {
        File javaClass = new File(filePath);
        try {
            Scanner scanner = new Scanner(javaClass);
            ArrayList<String> codes = new ArrayList<>();
            while (scanner.hasNext()) codes.add(scanner.nextLine());
            return codes.toArray(new String[]{});
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MyCFG getMyCFG() {
        return myCFG;
    }

    public void setMyCFG(MyCFG myCFG) {
        this.myCFG = myCFG;
    }
}
