package example;
import soot.*;
import soot.baf.BafASMBackend;
import soot.jimple.*;
import soot.options.Options;
import soot.util.*;
import java.io.*;
import java.util.*;


public class SootNoob1 {
    public static void main(String[] args) throws IOException {
        SootClass sClass;
        SootMethod method;


        // Resolve dependencies
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();

        // Declare 'public class HelloWorld'
        sClass = new SootClass("HelloWorld", Modifier.PUBLIC);

        // 'extends Object'
        sClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        Scene.v().addClass(sClass);

        // Create the method, public static void main(String[])
        method = new SootMethod("main",
                Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),
                VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);

        sClass.addMethod(method);

        // Create the method body
        {
            // create empty body
            JimpleBody body = Jimple.v().newBody(method);

            method.setActiveBody(body);
            Chain units = body.getUnits();
            Local arg, tmpRef;

            // Add some locals, java.lang.String l0
            arg = Jimple.v().newLocal("l0", ArrayType.v(RefType.v("java.lang.String"), 1));
            body.getLocals().add(arg);

            // Add locals, java.io.printStream tmpRef
            tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
            body.getLocals().add(tmpRef);

            // add "l0 = @parameter0"
            units.add(Jimple.v().newIdentityStmt(arg,
                    Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0)));

            // add "tmpRef = java.lang.System.out"
            units.add(Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
                    Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));

            // insert "tmpRef.println("Hello world!")"
            {
                SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
                units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v("Hello world!"))));
            }

            // insert "return"
            units.add(Jimple.v().newReturnVoidStmt());

        }

        // Write to class file
//        int java_version = Options.v().java_version();
//        String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_class);
//        OutputStream streamOut = new FileOutputStream(fileName);
//        BafASMBackend backend = new BafASMBackend(sClass, java_version);
//        backend.generateClassFile(streamOut);
//        streamOut.close();

//        String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_class);
//        OutputStream streamOut = new JasminOutputStream(
//                new FileOutputStream(fileName));
//        PrintWriter writerOut = new PrintWriter(
//                new OutputStreamWriter(streamOut));
//        JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
//        jasminClass.print(writerOut);
//        writerOut.flush();
//        streamOut.close();

//        String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_jimple);
//        OutputStream streamOut = new FileOutputStream(fileName);
//        PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
//        Printer.v().printTo(sClass, writerOut);
//        writerOut.flush();
//        streamOut.close();



    }
}
