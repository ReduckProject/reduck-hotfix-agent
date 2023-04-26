package net.reduck.hotfix.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Gin
 * @since 2023/4/26 13:51
 */
public class Launcher {
    static Log log = new Log();

    public static void premain(String agentArgs, Instrumentation inst) {
//        Properties properties = load("/tmp/agent.properties");
//
//        properties.getProperty("classNames").split()
        inst.addTransformer(new HotfixTransformer(new HashSet<String>()));
    }

    public static class HotfixTransformer implements ClassFileTransformer {
        private final Set<String> classNames;


        public HotfixTransformer(Set<String> classNames) {
            this.classNames = classNames;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            log.info("load : " + className);
            if (classNames == null || !classNames.contains(className)) {
                return null;
            }
            log.info("replace :" + className);
            return null;
        }
    }

    private static Properties load(String filePath) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

    private static class Log {
        private final File log = new File("/tmp/agent.log");
        private FileOutputStream os;

        {
            if (!log.getParentFile().exists()) {
                log.mkdirs();
            }

            try {
                os = new FileOutputStream(log, true);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void info(String message) {
            try {
                os.write(message.getBytes());
                os.write("\n".getBytes());
                os.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
