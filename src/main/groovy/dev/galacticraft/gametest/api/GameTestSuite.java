package dev.galacticraft.gametest.api;

import dev.galacticraft.gametest.impl.GameTestSuiteTarget;
import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testing.base.TestSuite;

public interface GameTestSuite extends TestSuite {
    void setSources(SourceSet sourceSet);

    SourceSet getSources();

    GameTestSuiteTarget create(String name);

    void create(String name, Action<GameTestSuiteTarget> config);

    @Override
    ExtensiblePolymorphicDomainObjectContainer<GameTestSuiteTarget> getTargets();

    Property<String> getTestType();
}
