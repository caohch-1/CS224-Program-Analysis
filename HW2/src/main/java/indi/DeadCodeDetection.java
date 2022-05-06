package indi;

import indi.ConstantPropagation.ConstantPropagation;
import indi.ConstantPropagation.ConstantPropagationFlow;
import indi.ConstantPropagation.ConstantPropagationValue;
import indi.LiveVariable.LiveVariableAnalysis;
import indi.Util.UtilCPValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.LookupSwitchStmt;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DeadCodeDetection {
    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");
    SootClass mainClass;
    SootMethod mainMethod;
    String classDir;

    Body jimpleBody;
    UnitGraph cfg;
    Map<Unit, Set<Local>> unitInMap;
    Map<Unit, Set<Local>> unitOutMap;
    String[] sourceCodes;
    Set<Integer> controlFlowUnreachableStmts;
    Set<Integer> unreachableBranchStmts;
    Set<Integer> deadAssignmentStmts;


    public DeadCodeDetection(String className, String methodName, String dir) {
        classDir = dir;
        setupSoot();

        try {
            mainClass = Scene.v().loadClassAndSupport(className);
        } catch (CompilationDeathException e) {
            logger.error(e.toString());
            System.exit(-1);
        }

        mainMethod = mainClass.getMethodByName(methodName);
        jimpleBody = mainMethod.retrieveActiveBody();
        cfg = new BriefUnitGraph(jimpleBody);
        unitInMap = new HashMap<>();
        unitOutMap = new HashMap<>();
        sourceCodes = readFile(classDir + "/" + className + ".java");
    }

    public static void main(String[] args) throws IOException {
        String mainMethodName = "main";
        String dir = "./src/test/java";
        if (args.length == 1) {
            dir = args[0];
        }

        File[] testJavaPaths = new File(dir).listFiles();
        if (testJavaPaths == null) {
            logger.error("No test file in " + dir + "\n");
            return;
        }

        for (File testFile : testJavaPaths) {
            if (testFile.getName().contains(".java")) {
                String mainClassName = testFile.getName().replace(".java", "");
                System.out.printf("=====Analysis for %s Start=====%n", mainClassName);
                DeadCodeDetection deadCodeDetection = new DeadCodeDetection(mainClassName, mainMethodName, dir);
                deadCodeDetection.doAnalysis();
                deadCodeDetection.readFile(testFile.getPath());
                deadCodeDetection.writeFile("./output/");
                System.out.printf("=====Analysis for %s Finish=====%n%n", mainClassName);
            } else {
                logger.warn(String.format("%s is not a java source code file", testFile.getName()));
            }
        }
    }

    void setupSoot() {
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(classDir));
    }

    void doAnalysis() {
        controlFlowUnreachableStmts = controlFlowUnreachableDetection();
        System.out.println("Control Flow Unreachable Code ID: " + controlFlowUnreachableStmts);

        deadAssignmentStmts = deadAssignmentDetection();
        System.out.println("Dead Assignment Code ID: " + deadAssignmentStmts);

        unreachableBranchStmts = unreachableBranchDetection();
        System.out.println("Unreachable Branch Code ID: " + unreachableBranchStmts);

        Set<Integer> res = new HashSet<>();
        res.addAll(controlFlowUnreachableStmts);
        res.addAll(unreachableBranchStmts);
        res.addAll(deadAssignmentStmts);

        List<Integer> sortedRes = new ArrayList<>(res);
        Collections.sort(sortedRes);

        System.out.println("All Dead Code ID: " + sortedRes);
    }

    Set<Integer> controlFlowUnreachableDetection() {
        Queue<Unit> unitQueue = new LinkedList<>(cfg.getHeads());
        Set<Integer> sourceVisited = new HashSet<>();
        Set<Integer> sourceCodeLines = new HashSet<>();
        for (Unit ut : cfg) {
            sourceCodeLines.add(ut.getJavaSourceStartLineNumber());
        }

        Set<Unit> visited = new HashSet<>();
        while (!unitQueue.isEmpty()) {
            Unit currUnit = unitQueue.poll();
            if (visited.contains(currUnit)) {
                continue;
            }
            visited.add(currUnit);

            sourceVisited.add(currUnit.getJavaSourceStartLineNumber());
            for (Unit succ : cfg.getSuccsOf(currUnit)) {
                unitQueue.offer(succ);
            }
        }

        sourceCodeLines.removeAll(sourceVisited);

        return sourceCodeLines;
    }

    Set<Integer> unreachableBranchDetection() {
        ConstantPropagation constantPropagation = new ConstantPropagation(cfg);
        Set<Pair<Unit, Unit>> unreachableBranchSet = new HashSet<>();

        for (Unit ut : cfg) {
            if (ut instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt) ut;
                Value value = ifStmt.getCondition();

                ConstantPropagationFlow CPMap = constantPropagation.getFlowBefore(ifStmt);
                ConstantPropagationValue CPValue = CPMap.calculate(value);

                if (CPValue != UtilCPValue.UNDEF && CPValue != UtilCPValue.NAC) {
                    if (CPValue.getValue() == 0) {
                        unreachableBranchSet.add(new Pair<>(ifStmt, ifStmt.getTarget()));
                    } else if (CPValue.getValue() == 1) {
                        unreachableBranchSet.add(new Pair<>(ifStmt, jimpleBody.getUnits().getSuccOf(ifStmt)));
                    }
                }
            }

            // Todo: switch Default condition order can influence the result. Not consider it here.
            if (ut instanceof LookupSwitchStmt) {
                LookupSwitchStmt switchStmt = (LookupSwitchStmt) ut;
                Value value = switchStmt.getKey();
                ConstantPropagationFlow CPMap = constantPropagation.getFlowBefore(switchStmt);
                ConstantPropagationValue CPValue = CPMap.calculate(value);

                if (CPValue != UtilCPValue.NAC && CPValue != UtilCPValue.UNDEF) {
                    for (int i = 0; i < switchStmt.getLookupValues().size(); i++) {
                        int lookupValue = switchStmt.getLookupValue(i);

                        if (CPValue.getValue() != lookupValue) {
                            unreachableBranchSet.add(new Pair<>(switchStmt, switchStmt.getTarget(i)));
                        }
                        if (CPValue.getValue() == lookupValue) {

                            unreachableBranchSet.add(new Pair<>(switchStmt, switchStmt.getDefaultTarget()));
                        }
                    }
                }
            }
        }


        Set<Unit> visited = new HashSet<>();
        Queue<Unit> queue = new LinkedList<>(cfg.getHeads());
        while (!queue.isEmpty()) {
            Unit currUnit = queue.poll();

            if (visited.contains(currUnit)) {
                continue;
            }
            visited.add(currUnit);

            for (Unit succUnit : cfg.getSuccsOf(currUnit)) {
                if (!unreachableBranchSet.contains(new Pair<>(currUnit, succUnit))) {
                    queue.add(succUnit);
                }
            }
        }

        Set<Integer> sourceVisited = new HashSet<>();
        for (Unit unit : visited) {
            if (!Objects.equals(unit.toString(), "nop")) sourceVisited.add(unit.getJavaSourceStartLineNumber());
        }

        Set<Integer> sourceCodeLines = new HashSet<>();
        for (Unit ut : cfg) {
            sourceCodeLines.add(ut.getJavaSourceStartLineNumber());
        }

        sourceCodeLines.removeAll(sourceVisited);

        return sourceCodeLines;
    }

    Set<Integer> deadAssignmentDetection() {
        LiveVariableAnalysis liveVariableAnalysis = new LiveVariableAnalysis(cfg);
        Set<Unit> deadAss = new HashSet<>();
        Set<Integer> assWithInvoke = new HashSet<>();
        for (Unit ut : cfg) {
            if (ut instanceof AssignStmt) {
                AssignStmt assignStmt = (AssignStmt) ut;
                Value value = assignStmt.getLeftOp();
                if (value instanceof Local) {
                    Local local = (Local) value;
                    FlowSet<Local> liveVariableSet = liveVariableAnalysis.getFlowAfter(ut);
                    if (!liveVariableSet.contains(local)) {
                        deadAss.add(ut);
                    }
                    if (assignStmt.containsInvokeExpr()) {
                        assWithInvoke.add(ut.getJavaSourceStartLineNumber());
                    }
                }
            }
        }

        Set<Integer> deadSource = new HashSet<>();
        for (Unit ut : deadAss) {
            deadSource.add(ut.getJavaSourceStartLineNumber());
        }
        deadSource.removeAll(assWithInvoke);
        return deadSource;
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

    // Todo: Read source file and write descriptions to dead code lines
    void writeFile(String outDir) throws IOException {

        Files.createDirectories(Paths.get(outDir));
        FileWriter writer = new FileWriter(outDir + mainClass.getName() + ".txt");
        boolean ifWrite = false;
        Stack<String> mainMethodStack = new Stack<>();
        for (int i = 0; i < sourceCodes.length; i++) {
            if (mainMethodStack.size() == 1 && ifWrite && sourceCodes[i].trim().equals("}")) {
                break;
            }

            if (ifWrite) {
                String line = String.format("Line %d : %s\t", i + 1, sourceCodes[i].trim());
                if (unreachableBranchStmts.contains(i + 1)) {
                    line += "unreachable branch\n";
                } else if (controlFlowUnreachableStmts.contains(i + 1)) {
                    line += "control-flow unreachable code\n";
                } else if (deadAssignmentStmts.contains(i + 1)) {
                    line += "dead assignment\n";
                } else {
                    line += "\n";
                }
                writer.write(line);
            }

            if (sourceCodes[i].contains("main")) {
                ifWrite = true;
                mainMethodStack.push("{");
            } else if (sourceCodes[i].contains("{") && ifWrite) {
                mainMethodStack.push("{");
            }

            if (sourceCodes[i].contains("}")) {
                mainMethodStack.pop();
            }

        }
        writer.flush();
        writer.close();

    }
}
