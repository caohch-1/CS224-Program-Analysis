package indi;

import indi.ConstantPropagation.ConstantPropagation;
import indi.ConstantPropagation.ConstantPropagationMap;
import indi.ConstantPropagation.ConstantPropagationValue;
import indi.ConstantPropagation.UtilCPValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;

import java.io.File;
import java.io.FileNotFoundException;
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

    public static void main(String[] args) {
        String mainMethodName = "main";
        if (args.length == 0) {
            String mainClassName = "UnreachableIfBranch";
//            String mainClassName = "DeadAssignment";
            String dir = "./src/test/java";
            DeadCodeDetection deadCodeDetection = new DeadCodeDetection(mainClassName, mainMethodName, dir);
            deadCodeDetection.doAnalysis();
        } else {
            System.out.println(args[0]);
        }
    }

    void setupSoot() {
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(classDir));
    }

    void doAnalysis() {
        Set<Integer> controlFlowUnreachableStmts = controlFlowUnreachableDetection();
        System.out.println("Control Flow Unreachable Code ID: " + controlFlowUnreachableStmts);

        Set<Integer> deadAssignmentStmts = deadAssignmentDetection();
        System.out.println("Dead Assignment Code ID: " + deadAssignmentStmts);

        Set<Integer> unreachableBranchStmts = unreachableBranchDetection();
        System.out.println("Unreachable Branch Code ID: " + unreachableBranchStmts);

        Set<Integer> res = new HashSet<>();
        res.addAll(controlFlowUnreachableStmts);
        res.addAll(unreachableBranchStmts);
        res.addAll(deadAssignmentStmts);

        System.out.println("All Dead Code ID: " + res);
    }

    Set<Integer> controlFlowUnreachableDetection() {
        Queue<Unit> unitQueue = new LinkedList<>(cfg.getHeads());
        Set<Integer> sourceVisited = new HashSet<>();
        Set<Integer> sourceCodeLines = new HashSet<>();
        for (Unit ut: cfg) {
            sourceCodeLines.add(ut.getJavaSourceStartLineNumber());
        }

        while (!unitQueue.isEmpty()) {
            Unit currUnit = unitQueue.poll();
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

        for (Unit ut: cfg) {
            if (ut instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt) ut;
                Value value = ifStmt.getCondition();

                ConstantPropagationMap CPMap = constantPropagation.getFlowBefore(ifStmt);
                ConstantPropagationValue CPValue = CPMap.calculate(value);

                if (CPValue != UtilCPValue.UNDEF && CPValue != UtilCPValue.NAC) {
                    if (CPValue.getValue() == 0) {
                        unreachableBranchSet.add(new Pair<Unit, Unit>(ifStmt, ifStmt.getTarget()));
                    } else if (CPValue.getValue() == 1) {
                        unreachableBranchSet.add(new Pair<>(ifStmt, jimpleBody.getUnits().getSuccOf(ifStmt)));
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

            for (Unit succUnit: cfg.getSuccsOf(currUnit)) {
                if (!unreachableBranchSet.contains(new Pair<Unit, Unit>(currUnit, succUnit))) {
                    queue.add(succUnit);
                }
            }
        }

        Set<Integer> sourceVisited = new HashSet<>();
        for (Unit unit: visited) {
            if (!Objects.equals(unit.toString(), "nop")) sourceVisited.add(unit.getJavaSourceStartLineNumber());
        }

        Set<Integer> sourceCodeLines = new HashSet<>();
        for (Unit ut: cfg) {
            sourceCodeLines.add(ut.getJavaSourceStartLineNumber());
        }

        sourceCodeLines.removeAll(sourceVisited);

        return sourceCodeLines;
    }

    Set<Integer> deadAssignmentDetection() {
        LiveVariableAnalysis liveVariableAnalysis = new LiveVariableAnalysis(cfg);
        Set<Unit> deadAss = new HashSet<>();
        for (Unit ut : cfg) {
            if (ut instanceof AssignStmt) {
                AssignStmt assignStmt = (AssignStmt) ut;
                Value value = assignStmt.getLeftOp();
                if (value instanceof Local) {
                    Local local = (Local) value;
                    FlowSet<Local> liveVariableSet = liveVariableAnalysis.getFlowAfter(ut);
                    if (!liveVariableSet.contains(local) && !assignStmt.containsInvokeExpr()) {
                        deadAss.add(ut);
                    }
                }
            }
        }
        Set<Integer> deadSource = new HashSet<>();
        for (Unit ut : deadAss) {
            deadSource.add(ut.getJavaSourceStartLineNumber());
        }
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
}
