package indi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LiveVariableAnalysis {
    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");
    MyCFG myCFG;
    SootClass mainClass;
    String classesDir;
    String outputDir = "./outputFiles/";

    public LiveVariableAnalysis(String className, String methodName, String dir) {
        classesDir = dir;
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
        logger.info("\n****************************************");

        logger.info(String.format("Soot Class: %s, Method: %s", mainClass.getName(), method.getName()));
        logger.info(String.format("Soot method locals: %s", jimpleBody.getLocals()));
    }

    public static void main(String[] args) throws IOException {
        String mainMethodName = "main";

        if (args.length == 0) {
            String mainClassName = "Calculate";
            LiveVariableAnalysis liveVariableAnalysis = new LiveVariableAnalysis(mainClassName, mainMethodName, "./src/test/java/");
            liveVariableAnalysis.doAnalysisAndShow();
        } else {
            File[] testJavaPaths = new File(args[0]).listFiles();
            if (testJavaPaths == null) {
                logger.error("No test file in "+args[0]+"\n");
                return;
            }
            Files.createDirectories(Paths.get("./output"));
            for (File testJava : testJavaPaths) {
                String mainClassName = testJava.getName().replace(".java", "");
                LiveVariableAnalysis liveVariableAnalysis = new LiveVariableAnalysis(mainClassName, mainMethodName, args[0]+"/");
                liveVariableAnalysis.doAnalysisAndShowWithArg(testJava.getPath());
            }
        }
    }

    void setupSoot() {
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(classesDir));

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
            Set<CFGNode> visited = new HashSet<>();
            Queue<CFGNode> queue = new LinkedList<>(getMyCFG().getTails());
            while (queue.size() != 0) {
                // Get predNodes for future travel
                CFGNode node = queue.poll();
                visited.add(node);
                ArrayList<CFGNode> predNodes = getMyCFG().getPredsOf(node);
                for (CFGNode predNode : predNodes) {
                    if (!visited.contains(predNode)) queue.offer(predNode);
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

                if (!Objects.equals(inSetBefore.toString(), node.getInSet().toString()) || !Objects.equals(outSetBefore.toString(), node.getOutSet().toString()))
                    ifChange = true;
            }
        }
        return iterNum;
    }

    void doAnalysisAndShow() throws IOException {
        String[] sourceCodes = readFile(classesDir + mainClass.getName() + ".java");

        int iterNum = doAnalysis();
        Map<Integer, String> map = new HashMap<>();
        System.out.printf("Iteration %d times\n", iterNum);
        int stmtID = 0, sourceID = 1;
        for (CFGNode cfgNode : myCFG.getCfgNodes()) {
            stmtID += 1;
            int newSourceID = cfgNode.getUnit().getJavaSourceStartLineNumber();
            if (newSourceID != sourceID) {
                sourceID = newSourceID;
                System.out.printf("\n===== Source[%d]: %s =====\n", sourceID, sourceCodes[sourceID - 1].replaceAll(" ", ""));
            }
            System.out.printf("StateM[%d]: %s,\nOutSet[%d]: %s\nInSet [%d]: %s\n", stmtID, cfgNode.getUnit(), stmtID, cfgNode.getOutSet(), stmtID, cfgNode.getInSet());
            map.put(sourceID, cfgNode.getOutSet().toString());
        }

        for (int i = 0; i < sourceCodes.length; i++) {
            String out = map.get(i + 1);
            if (out != null) sourceCodes[i] += "\t" + out + "\n";
            else sourceCodes[i] += "\n";
        }
        writeFile(outputDir + mainClass.getName() + ".java", sourceCodes);
    }

    void doAnalysisAndShowWithArg(String filePath) throws IOException {
        String[] sourceCodes = readFile(filePath);

        int iterNum = doAnalysis();

        Map<Integer, String> map = new HashMap<>();
        System.out.printf("Iteration %d times\n", iterNum);
        int stmtID = 0, sourceID = 1;
        for (CFGNode cfgNode : myCFG.getCfgNodes()) {
            stmtID += 1;
            int newSourceID = cfgNode.getUnit().getJavaSourceStartLineNumber();
            if (newSourceID != sourceID) {
                sourceID = newSourceID;
                System.out.printf("\n===== Source[%d]: %s =====\n", sourceID, sourceCodes[sourceID - 1].replaceAll(" ", ""));
            }
            System.out.printf("StateM[%d]: %s,\nOutSet[%d]: %s\nInSet [%d]: %s\n", stmtID, cfgNode.getUnit(), stmtID, cfgNode.getOutSet(), stmtID, cfgNode.getInSet());
            map.put(sourceID, cfgNode.getOutSet().toString());
        }

        for (int i = 0; i < sourceCodes.length; i++) {
            String out = map.get(i + 1);
            if (out != null) sourceCodes[i] += " " + out + "\n";
            else sourceCodes[i] += "\n";
        }
        writeFile("./output/" + mainClass.getName() + ".txt", sourceCodes);
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

    void writeFile(String filePath, String[] data) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        boolean ifWrite = false;
        for (String line : Arrays.copyOfRange(data, 0, data.length -2)) {
            if (ifWrite) {
                for (int i = 0; i < 8; i++) {
                    line = line.replaceFirst(" ", "");
                }
                writer.write(line);
            }
            if (line.contains("main")) {
                ifWrite = true;
            }
        }
        writer.flush();
        writer.close();
    }

    public MyCFG getMyCFG() {
        return myCFG;
    }

    public void setMyCFG(MyCFG myCFG) {
        this.myCFG = myCFG;
    }
}
