package jvm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.List;

/**
 * @author flowscolors
 * @date 2021-11-08 23:24
 */
public class Cmd {

    @Parameter(names = {"-?","-h","-help"}, description = "print help message", order = 3, help = true)
    boolean helpFlag = false;

    @Parameter(names = {"-v","-version"}, description = "print version and exit", order = 2)
    boolean versionFlag = false;

    @Parameter(names = "verbose", description = "enable verbose output", order = 5)
    boolean verboseClassFlag = false;

    @Parameter(names = {"-cp", "-classpath"}, description = "classpath", order = 1)
    String classpath;

    @Parameter(names = "-Xjre", description = "path to jre", order = 4)
    String jre;

    @Parameter(description = "main class and args")
    List<String> mainClassAndArgs;

    boolean ok;

    String getMainClass() {
        return mainClassAndArgs!=null && !mainClassAndArgs.isEmpty() ?
            mainClassAndArgs.get(0) : null;
    }

    List<String> getAppArgs() {
        return mainClassAndArgs!=null && mainClassAndArgs.size()>1 ?
            mainClassAndArgs.subList(1,mainClassAndArgs.size()) : null;
    }

    static Cmd parse(String[] argv){
        Cmd args = new Cmd();
        JCommander cmd = JCommander.newBuilder().addObject(args).build();
        cmd.parse(argv);
        args.ok = true;
        return args;
    }

}
