package jvm;

import jvm.classfile.ClassFile;
import jvm.classfile.MemberInfo;
import jvm.classpath.Classpath;
import jvm.rtda.Frame;
import jvm.rtda.LocalVars;
import jvm.rtda.OperandStack;
import jvm.rtda.Thread;
import jvm.rtda.heap.ClassLoader;
import jvm.rtda.heap.methodarea.Method;
import jvm.rtda.heap.methodarea.Class;

import java.util.Arrays;

/**
 * @author flowscolors
 * @date 2021-11-08 23:23
 */
//program arguments：-Xjre "D:\codeSource\jdk1.8.0_102\jre" D:\codeProject\javaProject\javalearncode\java-jvm\samll-jvm\target\test-classes\jvm\test\HelloWorld
// -Xjre "D:\codeSource\jdk1.8.0_102\jre" java.lang.String
public class Main {

    public static void main(String[] args) {
        Cmd cmd =Cmd.parse(args);
        if(!cmd.ok || cmd.helpFlag){
            System.out.println("Usage: <main class> [-options] class [args...]");
            return;
        }
        if(cmd.versionFlag){
            System.out.println("java version \"1.8.0\"");
            return;
        }
        startJVM(cmd);
        //startJVMForFrame(cmd);
    }

    private static void startJVM(Cmd cmd){
        Classpath classpath = new Classpath(cmd.jre, cmd.classpath);
        System.out.printf("classpath：%s class：%s args：%s\n", classpath, cmd.getMainClass(), cmd.getAppArgs());
        //获取className
        String className = cmd.getMainClass().replace(".", "/");
        //输出class文件的文件格式
        printClassFile(className,classpath,cmd.getMainClass());
        //把class文件加载成可被识别的classFile，并打印  only for print方法
        ClassFile classFile = loadClass(className, classpath);
        printClassInfo(classFile);
        //获得main方法，如果没有则报错，如果有则从main方法开始执行
        ClassLoader classLoader = new ClassLoader(classpath);
        Class mainClass = classLoader.loadClass(className);
        Method mainMethod = mainClass.getMainMethod();
        if (null == mainMethod) {
            throw new RuntimeException("Main method not found in class " + cmd.getMainClass());
        }
        new Interpret(mainMethod,cmd.verboseClassFlag);
    }

    //================================== unit 1 2 3 5字节码加载 解析部分 ====================================
    private static ClassFile loadClass(String className, Classpath classpath) {
        try {
            byte[] classData = classpath.readClass(className);
            return new ClassFile(classData);
        } catch (Exception e) {
            System.out.println("Could not find or load main class " + className);
            return null;
        }
    }

    //找到主函数入口方法 main方法的名字就是这个，所以只能有一个main函数
    private static MemberInfo getMainMethod(ClassFile cf) {
        if (cf == null) {
            return null;
        }
        MemberInfo[] methods = cf.methods();
        for (MemberInfo m : methods) {
            if ("main".equals(m.name()) && "([Ljava/lang/String;)V".equals(m.descriptor())) {
                return m;
            }
        }
        return null;
    }

    private static void printClassFile(String className, Classpath classpath,String mainClass) {
        try {
            byte[] classData = classpath.readClass(className);
            System.out.println("classData：");
            for (byte b : classData) {
                //16进制输出
                System.out.print(String.format("%02x", b & 0xff) + " ");
            }
        } catch (Exception e) {
            System.out.println("Could not find or load main class " + mainClass);
            e.printStackTrace();
        }
    }

    private static void printClassInfo(ClassFile cf) {
        System.out.println("version: " + cf.majorVersion() + "." + cf.minorVersion());
        System.out.println("constants count：" + cf.constantPool().getSize());
        System.out.format("access flags：0x%x\n", cf.accessFlags());
        System.out.println("this class：" + cf.className());
        System.out.println("super class：" + cf.superClassName());
        System.out.println("interfaces：" + Arrays.toString(cf.interfaceNames()));
        System.out.println("fields count：" + cf.fields().length);
        for (MemberInfo memberInfo : cf.fields()) {
            System.out.format("%s \t\t %s\n", memberInfo.name(), memberInfo.descriptor());
        }

        System.out.println("methods count: " + cf.methods().length);
        for (MemberInfo memberInfo : cf.methods()) {
            System.out.format("%s \t\t %s\n", memberInfo.name(), memberInfo.descriptor());
        }
    }

    //================================== unit 4 Java运行时部分 Java虚拟机栈 ====================================
    /*private static void startJVMForFrame(Cmd args) {
        Thread thread = new Thread();
        Frame frame = new Frame(thread,100, 100);
        test_localVars(frame.localVars());
        test_operandStack(frame.operandStack());
    }
*/
    private static void test_localVars(LocalVars vars){
        vars.setInt(0,100);
        vars.setInt(1,-100);
        System.out.println(vars.getInt(0));
        System.out.println(vars.getInt(1));
    }

    private static void test_operandStack(OperandStack ops){
        ops.pushInt(100);
        ops.pushInt(-100);
        ops.pushRef(null);
        System.out.println(ops.popRef());
        System.out.println(ops.popInt());
    }

}
