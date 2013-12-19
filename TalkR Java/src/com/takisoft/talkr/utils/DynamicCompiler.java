/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.takisoft.talkr.utils;

import com.takisoft.talkr.ai.Group;
import com.takisoft.talkr.ai.Programs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 *
 * @author RedMax
 */
public class DynamicCompiler {

    public static final String PREFIX_ASK = "ask_";
    public static final String PREFIX_ANSWER = "answer_";
    public final static String COMPILATION_PATH = "extclass";

    public static void compile(Group[] groups) {
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            System.err.println("Compiler unavailable");
            return;
        }

        //String code = "public class CustomProcessor { /*custom stuff*/ }";
        StringBuilder codeSb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader("src/com/takisoft/talkr/utils/DynamicProcessor.java"))) {
            String line;
            while ((line = br.readLine()) != null) {
                codeSb.append(line);
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        int idx = codeSb.lastIndexOf("}");
        codeSb.deleteCharAt(idx);

        /*
         public String ask_what_is_age(String input){
	
         }

         public String answer_what_is_age(){
            
         }
         */
        for (Group group : groups) {
            Programs prog;
            if ((prog = group.getPrograms()) != null) {
                if (prog.getAsk() != null) {
                    codeSb.append("public String ");
                    codeSb.append(PREFIX_ASK);
                    codeSb.append(group.getId());
                    codeSb.append("(String input){");
                    codeSb.append(prog.getAsk());
                    codeSb.append('}');
                    codeSb.append(' ');
                }

                if (prog.getAnswer() != null) {
                    codeSb.append("public String ");
                    codeSb.append(PREFIX_ANSWER);
                    codeSb.append(group.getId());
                    codeSb.append("(String input){");
                    codeSb.append(prog.getAnswer());
                    codeSb.append('}');
                    codeSb.append(' ');
                }
            }
        }

        codeSb.append('}');

        System.out.println(codeSb.toString());

        JavaSourceFromString jsfs = new JavaSourceFromString("DynamicProcessor", codeSb.toString());

        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(jsfs);

        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-d");
        options.add(COMPILATION_PATH);
        options.add("-cp");

        String cp = System.getProperty("user.dir");
        if (cp.contains("dist")) {
            cp += File.separatorChar + "TalkR Java.jar";
        } else {
            cp += File.separatorChar + "dist" + File.separatorChar + "TalkR Java.jar";
        }
        options.add(cp);

        StringWriter output = new StringWriter();
        boolean success = jc.getTask(output, null, null, options, null, fileObjects).call();
        if (success) {
            System.out.println("Class has been successfully compiled");
        } else {
            System.err.println("Compilation failed :" + output);
        }
    }

    public static Class getDynamicProcessor() {
        try {
            String path = System.getProperty("user.dir");
            File dir = new File(path, COMPILATION_PATH);
            if (!dir.exists()) {
                System.err.println(COMPILATION_PATH + " does not exist!");
                return null;
            }
            URL[] classes = {dir.toURI().toURL()};

            URLClassLoader child = new URLClassLoader(classes, Thread.currentThread().getContextClassLoader());

            String className = "com.takisoft.talkr.utils.DynamicProcessor";

            Class classToLoad = Class.forName(className, true, child);
            //Method method = classToLoad.getDeclaredMethod("hello", String.class);
            //Object instance = classToLoad.getConstructor(DynamicHelper.class).newInstance(new DynamicHelper(analyzer, resolver));
            //String result = (String) method.invoke(instance, "vazze");

            return classToLoad;
        } catch (MalformedURLException | ClassNotFoundException ex) {
            System.out.println(ex);
        }

        return null;
    }
}
