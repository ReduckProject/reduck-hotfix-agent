package net.reduck.hotfix.agent;

import net.reduck.internal.org.objectweb.asm.ClassReader;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Gin
 * @since 2023/4/26 13:51
 */
public class Launcher {
    static Log log = new Log();

    private static final Map<String, byte[]> hotfixClass = new HashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        File file = new File(agentArgs);
        init(file);

        if (hotfixClass.isEmpty()) {
            return;
        }

        log.info("args:" + agentArgs);
        inst.addTransformer(new HotfixTransformer(hotfixClass));

    }

    public static class HotfixTransformer implements ClassFileTransformer {
        private final Map<String, byte[]> classNames;

        public HotfixTransformer(Map<String, byte[]> classNames) {
            this.classNames = classNames;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            if (classNames == null || !classNames.containsKey(className)) {
                return null;
            }
            log.info("replace :" + className);
            return classNames.get(className);
        }
    }

    private static Properties load(String filePath) {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get(filePath)));
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

            try (InputStream in = res.openStream()) {
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
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("I/O exception reading class " + name, e);
        }
        byte[] data = new byte[p];

        System.arraycopy(b, 0, data, 0, p);
        return data;
    }

    public static byte[] readResource(File file) throws MalformedURLException {
        URL res = file.toURI().toURL();
        byte[] b;
        int p = 0;
        try {

            try (InputStream in = res.openStream()) {
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
            }
        } catch (IOException e) {
            throw new RuntimeException("Read " + file.getAbsolutePath() + " failed : " + e.getMessage());
        }
        byte[] data = new byte[p];

        System.arraycopy(b, 0, data, 0, p);
        return data;
    }

    private static void init(File root) {
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] subFiles = root.listFiles();
                if (subFiles != null) {
                    for (File file : subFiles) {
                        init(file);
                    }
                }
            } else {
                if (root.getName().endsWith(".class")) {
                    try {
                        byte[] classData = readResource(root);
                        hotfixClass.put(new ClassReader(classData).getClassName(), classData);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
