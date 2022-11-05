package dev.galacticraft.gametest.impl;

import net.fabricmc.loom.task.AbstractRunTask;
import org.gradle.api.internal.tasks.testing.TestExecutionSpec;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.util.Path;

public class GameTestTestExecutionSpec implements TestExecutionSpec {
    private final String name;
    private final AbstractRunTask runTask;
    private final Path identityPath;

    public GameTestTestExecutionSpec(AbstractRunTask from, ExecActionFactory factory) {
        this.runTask = from;
//        this.runTask = factory.newJavaExecAction();
////        ((ProcessForkOptions)from).copyTo(this.runTask);
////        ((JavaForkOptions)from).copyTo(this.runTask);
//        this.runTask.getMainModule().set(from.getMainModule().getOrNull());
//        this.runTask.getMainClass().set(from.getMainClass().getOrNull());
//        this.runTask.setWorkingDir(from.getWorkingDir());
//        if (from.getArgs() != null) this.runTask.setArgs(from.getArgs());
//        this.runTask.getArgumentProviders().clear();
//        this.runTask.getArgumentProviders().addAll(from.getArgumentProviders());
        this.runTask.classpath(this.runTask.getClasspath().getFiles()); // resolve deps NOW - we'll be running off-thread
//        this.runTask.environment(from.getEnvironment());
//        this.runTask.setExecutable(Objects.requireNonNullElseGet(from.getExecutable(), () -> Jvm.current().getJavaExecutable().getAbsolutePath()));
//        this.runTask.setSystemProperties(from.getSystemProperties());
//        this.runTask.setMinHeapSize(from.getMinHeapSize());
//        this.runTask.setMaxHeapSize(from.getMaxHeapSize());
//        this.runTask.setEnableAssertions(from.getEnableAssertions());
//        this.runTask.setJvmArgs(from.getJvmArgs());
//        this.runTask.setDefaultCharacterEncoding(from.getDefaultCharacterEncoding());
//        this.runTask.getJvmArgumentProviders().clear();
//        this.runTask.getJvmArgumentProviders().addAll(from.getJvmArgumentProviders());
//        this.runTask.setDebug(from.getDebug());
//        this.runTask.setBootstrapClasspath(from.getBootstrapClasspath());

        this.name = from.getName();
        this.identityPath = from.getIdentityPath();
    }

    public AbstractRunTask getExec() {
        return this.runTask;
    }

    public String getName() {
        return this.name;
    }

    public Path getIdentityPath() {
        return this.identityPath;
    }
}
