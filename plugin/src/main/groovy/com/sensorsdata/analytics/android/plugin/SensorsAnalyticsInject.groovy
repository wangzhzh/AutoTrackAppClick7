package com.sensorsdata.analytics.android.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.MethodInfo
import javassist.bytecode.annotation.Annotation
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class SensorsAnalyticsInject {
    private static ClassPool pool = ClassPool.getDefault()
    private static
    final String SDK_HELPER = "com.sensorsdata.analytics.android.sdk.SensorsDataAutoTrackHelper"

    static void appendClassPath(String libPath) {
        pool.appendClassPath(libPath)
    }

    /**
     * 这里需要将jar包先解压，注入代码后再重新生成jar包
     * @path jar包的绝对路径
     */
    static File injectJar(String path, Project project) {
        appendClassPath(path)

        if (path.endsWith(".jar")) {
            pool.appendClassPath(project.android.bootClasspath[0].toString())
            File jarFile = new File(path)
            // jar包解压后的保存路径
            String jarZipDir = jarFile.getParent() + "/" + jarFile.getName().replace('.jar', '')

            // 解压jar包, 返回jar包中所有class的完整类名的集合（带.class后缀）
            List<File> classNameList = unzipJar(path, jarZipDir)

            // 删除原来的jar包
            jarFile.delete()

            // 注入代码
            pool.appendClassPath(jarZipDir)
            for (File classFile : classNameList) {
                injectClass(classFile, jarZipDir)
            }

            // 重新打包jar
            zipJar(jarZipDir, path)

            // 删除目录
            FileUtils.deleteDirectory(new File(jarZipDir))

            return jarFile
        }

        return null
    }

    private static void injectClass(File classFile, String path) {
        String filePath = classFile.absolutePath
        if (!filePath.endsWith(".class")) {
            return
        }

        if (!filePath.contains('R$')
                && !filePath.contains('R2$')
                && !filePath.contains('R.class')
                && !filePath.contains('R2.class')
                && !filePath.contains("BuildConfig.class")) {
            int index = filePath.indexOf(path)
            String className = filePath.substring(index + path.length() + 1, filePath.length() - 6).replaceAll("/", ".")
            if (!className.startsWith("android")) {
                try {
                    CtClass ctClass = pool.getCtClass(className)

                    //解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    boolean modified = false

                    CtClass[] interfaces = ctClass.getInterfaces()

                    if (interfaces != null) {
                        Set<String> interfaceList = new HashSet<>()
                        for (CtClass c1 : interfaces) {
                            interfaceList.add(c1.getName())
                        }

                        for (CtMethod currentMethod : ctClass.getDeclaredMethods()) {
                            MethodInfo methodInfo = currentMethod.getMethodInfo()
                            AnnotationsAttribute attribute = (AnnotationsAttribute) methodInfo
                                    .getAttribute(AnnotationsAttribute.visibleTag)
                            if (attribute != null) {
                                for (Annotation annotation : attribute.annotations) {
                                    if ("@com.sensorsdata.analytics.android.sdk.SensorsDataTrackViewOnClick" == annotation.toString()) {
                                        if ('(Landroid/view/View;)V' == currentMethod.getSignature()) {
                                            currentMethod.insertAfter(SDK_HELPER + ".trackViewOnClick(\$1);")
                                            modified = true
                                            break
                                        }
                                    }
                                }
                            }

                            String methodSignature = currentMethod.name + currentMethod.getSignature()

                            if ('onContextItemSelected(Landroid/view/MenuItem;)Z' == methodSignature) {
                                currentMethod.insertAfter(SDK_HELPER + ".trackViewOnClick(\$0,\$1);")
                                modified = true
                            } else if ('onOptionsItemSelected(Landroid/view/MenuItem;)Z' == methodSignature) {
                                currentMethod.insertAfter(SDK_HELPER + ".trackViewOnClick(\$0,\$1);")
                                modified = true
                            } else {
                                SensorsAnalyticsMethodCell methodCell = SensorsAnalyticsConfig.isMatched(interfaceList, methodSignature)
                                if (methodCell != null) {
                                    StringBuffer stringBuffer = new StringBuffer()
                                    stringBuffer.append(SDK_HELPER)
                                    stringBuffer.append(".trackViewOnClick(")
                                    for (int i = methodCell.getParamStart(); i < methodCell.getParamStart() + methodCell.getParamCount(); i++) {
                                        stringBuffer.append("\$")
                                        stringBuffer.append(i)
                                        if (i != (methodCell.getParamStart() + methodCell.getParamCount() - 1)) {
                                            stringBuffer.append(",")
                                        }
                                    }
                                    stringBuffer.append(");")
                                    currentMethod.insertAfter(stringBuffer.toString())
                                    modified = true
                                }
                            }
                        }
                    }

                    if (modified) {
                        ctClass.writeFile(path)
                        ctClass.detach()//释放
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    static void injectDir(String path, Project project) {
        try {
            pool.appendClassPath(path)
            /**加入android.jar，不然找不到android相关的所有类*/
            pool.appendClassPath(project.android.bootClasspath[0].toString())

            File dir = new File(path)
            if (dir.isDirectory()) {
                dir.eachFileRecurse { File file ->
                    injectClass(file, path)
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    /**
     * 将该jar包解压到指定目录
     * @param jarPath jar包的绝对路径
     * @param destDirPath jar包解压后的保存路径
     * @return List < File >
     */
    static List<File> unzipJar(String jarPath, String destDirPath) {
        List<File> fileList = new ArrayList<>()
        if (jarPath.endsWith('.jar')) {
            JarFile jarFile = new JarFile(jarPath)
            Enumeration<JarEntry> jarEntries = jarFile.entries()
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement()
                if (jarEntry.directory) {
                    continue
                }
                String entryName = jarEntry.getName()
                String outFileName = destDirPath + "/" + entryName
                File outFile = new File(outFileName)
                fileList.add(outFile)
                outFile.getParentFile().mkdirs()
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                FileOutputStream fileOutputStream = new FileOutputStream(outFile)
                fileOutputStream << inputStream
                fileOutputStream.close()
                inputStream.close()
            }
            jarFile.close()
        }

        return fileList
    }

    /**
     * 重新打包jar
     * @param packagePath 将这个目录下的所有文件打包成jar
     * @param destPath 打包好的jar包的绝对路径
     */
    static void zipJar(String packagePath, String destPath) {
        File file = new File(packagePath)
        JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(destPath))
        file.eachFileRecurse { File f ->
            String entryName = f.getAbsolutePath().substring(file.absolutePath.length() + 1)
            outputStream.putNextEntry(new ZipEntry(entryName))
            if (!f.directory) {
                InputStream inputStream = new FileInputStream(f)
                outputStream << inputStream
                inputStream.close()
            }
        }
        outputStream.close()
    }
}