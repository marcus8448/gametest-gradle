package dev.galacticraft.gametest.impl;

import org.gradle.api.internal.tasks.testing.DefaultTestSuiteDescriptor;

final class RootTestSuiteDescriptor extends DefaultTestSuiteDescriptor {
    RootTestSuiteDescriptor(Object id, String name) {
        super(id, name);
    }
}
