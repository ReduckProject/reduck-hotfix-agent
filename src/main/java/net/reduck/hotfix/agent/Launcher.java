package net.reduck.hotfix.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.*;

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
        Map<String, String> map = new HashMap<String, String>();
        map.put("net.reduck.api.doc.descriptor.Agent".replace(".", "/"), "/Users/zhanjinkai/Documents/GitHub/reduck-hotfix-agent/class/Agent.class");
        inst.addTransformer(new HotfixTransformer(map));
    }

    public static class HotfixTransformer implements ClassFileTransformer {
        private final Map<String, String> classNames;


        public HotfixTransformer(Map<String, String> classNames) {
            this.classNames = classNames;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            log.info("load : " + className);
            if (classNames == null || !classNames.containsKey(className)) {
                return null;
            }
            log.info("replace :" + className);
            try {
                return readResource(className, classNames.get(className));
            } catch (Exception e) {
                log.info("error :" + e.getMessage());
                return null;
            }
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

    public static byte[] readResource(String name, String path) throws MalformedURLException, ClassNotFoundException {
        URL res = new File(path).toURI().toURL();
        byte[] b;
        int p = 0;
        try {
            InputStream in = res.openStream();

            try {
                b = new byte[65536];
                while (true) {
                    int r = in.read(b, p, b.length - p);
                    if (r == -1) break;
                    p += r;
                    if (p == b.length) {
                        byte[] nb = new byte[b.length * 2];
                        System.arraycopy(b, 0, nb, 0, p);
                        b = nb;
                    }
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("I/O exception reading class " + name, e);
        }
        byte[] data = new byte[p];

        System.arraycopy(b, 0, data, 0, p);
        return data;
    }
}
