package dev.galacticraft.gametest.impl;

import dev.galacticraft.gametest.api.GameTestSuite;
import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.attributes.TestSuiteType;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

import javax.inject.Inject;

public abstract class GameTestSuiteImpl implements GameTestSuite {
    private final ExtensiblePolymorphicDomainObjectContainer<GameTestSuiteTarget> targets;
    private final String name;
    private SourceSet sourceSet;

    public GameTestSuiteImpl(String name) {
        this.name = name;
        this.targets = getObjectFactory().polymorphicDomainObjectContainer(GameTestSuiteTarget.class);
        this.targets.registerBinding(GameTestSuiteTarget.class, GameTestSuiteTargetImpl.class);
        getTestType().set(TestSuiteType.UNIT_TEST);
    }

    @Override
    public void setSources(SourceSet sourceSet) {
        this.sourceSet = sourceSet;
        if (sourceSet.getName().equals("main")) {
            getProject().getDependencies().add("modRuntimeOnly", getProject().files(getProject().getBuildDir().toPath().resolve("tmp/gametest-gradle-0.0.0.jar")));
        } else {
//            getProject().getExtensions().getByType(LoomGradleExtensionAPI.class).createRemapConfigurations(sourceSet);
            getProject().getDependencies().add("mod" + Character.toUpperCase(sourceSet.getName().charAt(0)) + sourceSet.getName().substring(1) + "RuntimeOnly", getProject().files(getProject().getBuildDir().toPath().resolve("tmp/gametest-gradle-0.0.0.jar")));
        }
    }

    @Override //IJ needs this method for some reason
    public SourceSet getSources() {
        return this.sourceSet;
    }

    @Inject
    public abstract ObjectFactory getObjectFactory();

    @Inject
    public abstract Project getProject();

    @Override
    public GameTestSuiteTarget create(String name) {
        GameTestSuiteTarget gameTestSuiteTarget = this.targets.create(name, GameTestSuiteTarget.class);
        gameTestSuiteTarget.runSettings(s -> s.source(this.sourceSet));
        return gameTestSuiteTarget;
    }

    @Override
    public void create(String name, Action<GameTestSuiteTarget> config) {
        config.execute(create(name));
    }

    @Override
    public ExtensiblePolymorphicDomainObjectContainer<GameTestSuiteTarget> getTargets() {
        return this.targets;
    }

    @Override
    public abstract Property<String> getTestType();

    @Override
    public String getName() {
        return this.name;
    }
}
