package dev.galacticraft.gametest.impl;

import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import org.gradle.api.Action;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testing.base.TestSuiteTarget;

public interface GameTestSuiteTarget extends TestSuiteTarget {
    void runSettings(Action<RunConfigSettings> action);

    TaskProvider<GameTestRunTask> getTestTask();
}
