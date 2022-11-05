package dev.galacticraft.gametest.impl;

import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.Project;
import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public class GameTestSuiteTargetImpl implements GameTestSuiteTarget, Buildable {
    private final String name;
    private final TaskProvider<GameTestRunTask> testTask;
    private final RunConfigSettings settings;

    @Inject
    public GameTestSuiteTargetImpl(String name, TaskContainer tasks, Project project) {
        this.name = name;
        this.settings = new RunConfigSettings(project, name);
        this.testTask = tasks.register(name, GameTestRunTask.class, name, this.settings);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void runSettings(Action<RunConfigSettings> action) {
        action.execute(this.settings);
    }

    @Override
    public TaskProvider<GameTestRunTask> getTestTask() {
        return this.testTask;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return new AbstractTaskDependency() {
            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                context.add(testTask);
                context.add(settings.getSource(settings.getProject()));
            }
        };
    }
}
