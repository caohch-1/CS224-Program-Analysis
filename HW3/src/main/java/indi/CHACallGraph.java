package indi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.options.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CHACallGraph {
    private final static Logger logger = LoggerFactory.getLogger("LVA Logger");
    SootClass mainClass;
    String classDir;
    String[] sourceCodes;

    FastHierarchy chaHierarchy;
    baseCG cg;

    public CHACallGraph(String className, String dir) {
        classDir = dir;
        setupSoot();

        try {
            mainClass = Scene.v().loadClassAndSupport(className);
        } catch (CompilationDeathException e) {
            logger.error(e.toString());
            System.exit(-1);
        }

        sourceCodes = readFile(classDir + "/" + className + ".java");
        chaHierarchy = new FastHierarchy();
        cg = new baseCG();

        Queue<SootMethod> worklist = new LinkedList<>(cg.startPoints);
        while (!worklist.isEmpty()) {
            SootMethod sootMethod = worklist.poll();
            if (!sootMethod.hasActiveBody()) continue;

            Collection<Unit> callSites = cg.getCallSite(sootMethod);
            for (Unit callSite : callSites) {
                Set<SootMethod> callees = resolve(callSite);
                for (SootMethod callee : callees) {
                    if (!cg.reachableMethods.contains(callee)) worklist.add(callee);
                    cg.addEdge(callSite, callee, MethodHelper.getMethodKind(callSite));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
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
                StringBuilder resBuf = new StringBuilder();

                String mainClassName = testFile.getName().replace(".java", "");
                System.out.printf("=====Analysis for %s Start=====%n", mainClassName);
                CHACallGraph chaCallGraph = new CHACallGraph(mainClassName, dir);

                StringBuilder rcOut = new StringBuilder();
                int rcNum = 0;
                for (SootMethod rm : chaCallGraph.cg.reachableMethods) {
                    if (rm.getSignature().equals("<java.lang.Object: void <init>()>")) continue;
                    rcOut.append(rm.getSignature()).append("\n");
                    rcNum += 1;
                }
                System.out.printf("#reachable methods:%d\n", rcNum);
                System.out.print(rcOut);

                StringBuilder edgeOut = new StringBuilder();
                int edgeNum = 0;
                for (SootMethod rm : chaCallGraph.cg.reachableMethods) {
                    Set<Edge> edgeSet = chaCallGraph.cg.getCallOut(rm);
                    Map<Integer, Set<String>> line2calleSet = new HashMap<>();

                    for (Edge callEdge : edgeSet) {
                        if (callEdge.callee.getSignature().equals("<java.lang.Object: void <init>()>")) continue;

                        if (!line2calleSet.containsKey(callEdge.callSite.getJavaSourceStartLineNumber())) {
                            line2calleSet.put(callEdge.callSite.getJavaSourceStartLineNumber(), new HashSet<>());
                            edgeNum += 1;
                        }
                        line2calleSet.get(callEdge.callSite.getJavaSourceStartLineNumber()).add(callEdge.callee.getSignature());
                    }

                    for (int key : line2calleSet.keySet()) {
                        edgeOut.append(String.format("Line %d: %s -> %s\n", key, rm.getSignature(), line2calleSet.get(key)));
                    }
                }
                System.out.printf("\n#call graph edges:%d\n", edgeNum);
                System.out.print(edgeOut);

                Scene.v().removeClass(chaCallGraph.mainClass);
                Set<SootClass> toBeRemove = new HashSet<>(Scene.v().getApplicationClasses());
                for (SootClass cls : toBeRemove) {
                    Scene.v().removeClass(cls);
                }
                System.out.printf("=====Analysis for %s Finish=====%n%n", mainClassName);

                resBuf.append(String.format("#reachable methods:%d\n", rcNum)).append(rcOut).append(String.format("\n#call graph edges:%d\n", edgeNum)).append(edgeOut);
                chaCallGraph.writeFile(resBuf, "./output/");
            } else {
                logger.warn(String.format("%s is not a java source code file", testFile.getName()));
            }
        }
    }

    SootMethod dispatch(SootClass sootClass, SootMethod methodInput) {
        for (SootMethod method : sootClass.getMethods()) {
            if (!method.isAbstract()) {
                if (method.getSubSignature().equals(methodInput.getSubSignature())) {
                    return method;
                }
            }
        }

        SootClass superClass = sootClass.getSuperclassUnsafe();
        if (superClass != null) {
            return dispatch(superClass, methodInput);
        }
        return null;
    }

    Set<SootMethod> resolve(Unit unit) {
        Stmt stmt = (Stmt) unit;
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        SootMethod sootMethod = invokeExpr.getMethod();
        SootClass sootClass = sootMethod.getDeclaringClass();

        MethodHelper kind = MethodHelper.getMethodKind(unit);

        switch (kind) {
            case STATIC:
                return Collections.singleton(invokeExpr.getMethod());
            case SPECIAL:
                SootMethod dispatch = dispatch(sootClass, sootMethod);
                return dispatch == null ? Collections.emptySet() : Collections.singleton(dispatch);
        }

        Set<SootClass> classes;
        if (sootClass.isInterface()) {
            classes = chaHierarchy.getAllImplementersOfInterface(sootClass);
        } else {
            Type declaredType = invokeExpr.getUseBoxes().get(0).getValue().getType();
            SootClass declaredClass = null;
            for (SootClass cls : Scene.v().getApplicationClasses()) {
                if (Objects.equals(cls.getName(), declaredType.toString())) {
                    declaredClass = cls;
                }
            }

            if (declaredClass == null) logger.error(String.format("Can't find Class %s\n", declaredType));

            Stack<SootClass> subClasses = new Stack<>();
            subClasses.addAll(chaHierarchy.getSubclassesOf(declaredClass));
            classes = new HashSet<>(chaHierarchy.getSubclassesOf(declaredClass));
            while (!subClasses.empty()) {
                SootClass curr = subClasses.pop();
                subClasses.addAll(chaHierarchy.getSubclassesOf(curr));
                classes.addAll(chaHierarchy.getSubclassesOf(curr));
            }
            classes.add(declaredClass);
        }

        Set<SootMethod> res = new HashSet<>();
        for (SootClass cls : classes) {
            SootMethod dispatch = dispatch(cls, sootMethod);
            if (dispatch != null) res.add(dispatch);
        }

        return res;
    }

    void setupSoot() {
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(classDir));
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

    void writeFile(StringBuilder strBuff, String outDir) throws IOException {
        Files.createDirectories(Paths.get(outDir));
        FileWriter writer = new FileWriter(outDir + mainClass.getName() + ".txt");
        writer.write(strBuff.toString());
        writer.flush();
        writer.close();
    }
}
