package com.sensorsdata.analytics.android.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class SensorsAnalyticsTransform extends Transform {
    private static Project project

    SensorsAnalyticsTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "SensorsAnalyticsAutoTrack"
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * @return
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        if (!incremental) {
            outputProvider.deleteAll()
        }

        /**Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历 */
        inputs.each { TransformInput input ->
            /**遍历 jar*/
            input.jarInputs.each { JarInput jarInput ->
                /**重命名输出文件（同目录copyFile会冲突）*/
                String destName = jarInput.file.name

                /**截取文件路径的 md5 值重命名输出文件,因为可能同名,会覆盖*/
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                /** 获取 jar 名字*/
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                File copyJarFile = SensorsAnalyticsInject.injectJar(jarInput.file.absolutePath, project)
                def dest = outputProvider.getContentLocation(destName + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(copyJarFile, dest)

                context.getTemporaryDir().deleteDir()
            }

            /**遍历目录*/
            input.directoryInputs.each { DirectoryInput directoryInput ->
                SensorsAnalyticsInject.injectDir(directoryInput.file.absolutePath, project)

                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                /**将input的目录复制到output指定目录*/
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }
}