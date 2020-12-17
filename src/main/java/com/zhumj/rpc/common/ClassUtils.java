package com.zhumj.rpc.common;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {

    public static <T> T getInstanceByInterfaceName(String interfaceName) {

        try {
            Class<?> aClass = Class.forName(interfaceName);
            List<Class<?>> allClassByInterface = getAllClassByInterface(aClass);
            for (Class clazz: allClassByInterface) {
                Object o = clazz.newInstance();
                return (T)o;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
     * 获取指定接口的所有实现实例
     */
    public static List<Object> getAllObjectByInterface(Class<?> c)
            throws InstantiationException, IllegalAccessException {
        List<Object> list = new ArrayList<Object>();
        List<Class<?>> classes = getAllClassByInterface(c);
        for (int i = 0; i < classes.size(); i++) {
            list.add(classes.get(i).newInstance());
        }
        return list;
    }

    /*
     * 获取指定接口的实例的Class对象
     */
    public static List<Class<?>> getAllClassByInterface(Class<?> c) {
        // 如果传入的参数不是接口直接结束
        if (!c.isInterface()) {
            return null;
        }

        // 获取当前包名
        String packageName = c.getPackage().getName();
        List<Class<?>> allClass = null;
        try {
            allClass = getAllClassFromPackage(packageName);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        ArrayList<Class<?>> list = new ArrayList<Class<?>>();
        for (int i = 0; i < allClass.size(); i++) {
            if (c.isAssignableFrom(allClass.get(i))) {
                if (!c.equals(allClass.get(i))) {
                    list.add(allClass.get(i));
                }
            }
        }

        return list;
    }

    private static List<Class<?>> getAllClassFromPackage(String packageName) throws IOException, ClassNotFoundException{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    /*
     * 获取项目某路径下的所有类
     */
    public static List<String> getClassNameByFile(String fileSearchPath) {
        List<String> classNames = new ArrayList<String>();

        File file = new File(fileSearchPath);
        File[] childFiles = file.listFiles();
        for(File childFile : childFiles) {
            if(childFile.isDirectory()) {
                classNames.addAll(getClassNameByFile(childFile.getPath()));
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    String className = childFilePath.substring(childFilePath.lastIndexOf("\\bin\\") + 1,
                            childFilePath.length()).replaceAll("\\\\", ".");
                    className = className.substring(4, className.indexOf(".class"));
                    classNames.add(className);
                }
            }
        }

        return classNames;
    }

    /*
     * 从jar包中获取某路径下的所有类
     */
    public static List<String> getClassNameByJar(JarFile jarFile, String packageName) {
        List<String> classNames = new ArrayList<String>();
        Enumeration<JarEntry> entrys = jarFile.entries();
        while (entrys.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) entrys.nextElement();
            String entryName = jarEntry.getName();
            if(entryName.endsWith(".class")) {
                String className = entryName.replace("/", ".");
                className = className.substring(0, className.indexOf(".class"));
                classNames.add(className);
            }

        }
        return classNames;
    }
}
