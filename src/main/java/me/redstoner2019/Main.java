package me.redstoner2019;

import me.redstoner2019.odmsg.client.GUI;
import me.redstoner2019.odmsg.server.ServerConnector;

public class Main {
    public static void main(String[] args) {
        if(args.length == 1){
            if(args[0].equals("server")){
                new ServerConnector();
            } else {
                new GUI();
            }
            /*System.err.println("An exception has occurred in the compiler (21.0.2). Please file a bug against the Java compiler via the Java bug reporting page (https://bugreport.java.com) after checking the Bug Database (https://bugs.java.com) for \nduplicates. Include your program, the following diagnostic, and the parameters passed to the java compiler in your report. Thank you.");
            System.err.println("java.lang.NullPointerException: Cannot invoke \"com.sun.tools.javac.util.RichDiagnosticFormatter$ClassNameSimplifier.simplify(com.sun.tools.javac.code.Symbol)\" because \"this.this$nameSimplifier\" is null");
            System.exit(1);*/
        }
    }
}