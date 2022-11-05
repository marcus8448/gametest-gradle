package dev.galacticraft.gametest.impl;

import net.fabricmc.loom.configuration.ide.RunConfig;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.task.AbstractRunTask;
import org.gradle.StartParameter;
import org.gradle.api.Project;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.classpath.ModuleRegistry;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.AbstractTestTask;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.time.Clock;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.worker.WorkerProcessFactory;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.function.Function;

public abstract class GameTestRunTask extends AbstractTestTask {
    private final TaskProvider<? extends AbstractRunTask> runTask;
    private RunConfig config = null;

    @Inject
    public GameTestRunTask(String name, RunConfigSettings settings, TaskContainer tasks) {
        super();
        getTestLogging().events(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED);
        getExtensions().getExtraProperties().set("idea.internal.test", "true"); //why cant IJ just test for AbstractTestTask??
        setGroup(JavaBasePlugin.VERIFICATION_GROUP);
//        this.dependsOn(tasks.named("copyGametestModFile"));
        this.setDescription("Runs the gametest (" + name + ") suite.");

        Path test_output = getProject().getBuildDir().toPath().resolve("test_output");
        getBinaryResultsDirectory().set(test_output.resolve("bin").toFile()); //todo: what is this?
        getReports().getHtml().getOutputLocation().set(test_output.resolve("html").toFile());
        getReports().getJunitXml().getOutputLocation().set(test_output.resolve("junit").toFile());

        this.runTask = tasks.register("run" + Character.toUpperCase(name.charAt(0)) + name.substring(1), RunGametestTask.class, (Function<Project, RunConfig>) project -> {
            if (this.config == null) this.config = RunConfig.runConfig(project, settings);
            return this.config;
        });
        this.dependsOn(settings.getSource(getProject()).getOutput());
    }

    @Override
    protected GameTestTestExecutor createTestExecuter() {
        return new GameTestTestExecutor(getProcessBuilderFactory(), getActorFactory(), getModuleRegistry(),
                getServices().get(WorkerLeaseService.class),
                getServices().get(StartParameter.class).getMaxWorkerCount(),
                getServices().get(Clock.class),
                getServices().get(DocumentationRegistry.class),
                getExecActionFactory(),
                (DefaultTestFilter) getFilter());
    }

    @Inject
    public ModuleRegistry getModuleRegistry() {
        throw new UnsupportedOperationException("missing gradle injection magic?");
    }

    @Inject
    public ActorFactory getActorFactory() {
        throw new UnsupportedOperationException("missing gradle injection magic?");
    }

    @Inject
    public WorkerProcessFactory getProcessBuilderFactory() {
        throw new UnsupportedOperationException("missing gradle injection magic?");
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException("missing gradle injection magic?");
    }

    @Override
    protected GameTestTestExecutionSpec createTestExecutionSpec() {
        return new GameTestTestExecutionSpec(this.runTask.get(), getExecActionFactory());
    }

    @Internal
    public TaskProvider<? extends AbstractRunTask> getRunTask() {
        return runTask;
    }

    @Override
    @TaskAction
    public void executeTests() {
        super.executeTests();
    }
}
