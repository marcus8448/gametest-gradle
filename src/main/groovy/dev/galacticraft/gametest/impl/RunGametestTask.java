package dev.galacticraft.gametest.impl;

import net.fabricmc.loom.configuration.ide.RunConfig;
import net.fabricmc.loom.task.AbstractRunTask;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.util.function.Function;

public class RunGametestTask extends AbstractRunTask {
    @Inject
    public RunGametestTask(Function<Project, RunConfig> configProvider) {
        super(configProvider);
    }
}
