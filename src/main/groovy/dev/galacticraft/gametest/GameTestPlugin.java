package dev.galacticraft.gametest;

import dev.galacticraft.gametest.api.GameTestSuite;
import dev.galacticraft.gametest.impl.GameTestSuiteImpl;
import dev.galacticraft.gametest.impl.GameTestSuiteTarget;
import external.org.jetbrains.plugins.gradle.IJTestEventLogger;
import org.apache.tools.ant.BuildException;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.testing.AbstractTestTask;
import org.gradle.testing.base.TestSuite;
import org.gradle.testing.base.TestingExtension;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class GameTestPlugin implements Plugin<Project> {
    private final Map<String, TestSuite> testTypesInUse = new HashMap<>(1);

    @Override
    public void apply(Project project) {
        Map<String, String> map = new HashMap<>(1);
        map.put("plugin", "fabric-loom");
        project.apply(map);

//        project.getExtensions().create(GameTestExtension.class, "gametest", GameTestExtensionImpl.class, project);
        ExtensiblePolymorphicDomainObjectContainer<TestSuite> suites = project.getExtensions().getByType(TestingExtension.class).getSuites();
        suites.registerBinding(GameTestSuite.class, GameTestSuiteImpl.class);
        suites.register("gametest", GameTestSuite.class);
        configureTestDataElementsVariants(project);

        IJTestEventLogger.appendTests(project.getGradle());

        try {
            Files.copy(project.zipTree(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()).getFiles().stream().filter(f -> f.getName().equals("gametest-gradle-0.0.0.jar")).findFirst().get().toPath(), project.getBuildDir().toPath().resolve("tmp/gametest-gradle-0.0.0.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

//        String s;
//        for (File allInitScript : project.getGradle().getStartParameter().getAllInitScripts()) {
//            if (allInitScript.getName().endsWith("ijtestinit.gradle")) {
//                s = allInitScript.toURI().toString();
//            }
//        }
//        ClassLoaderScope child = ((GradleInternal) project.getGradle()).getClassLoaderScope().createChild("gametest-init");
//        try {
//            Class<?> aClass = child.getExportClassLoader().loadClass("org.jetbrains.plugins.gradle.external.org.jetbrains.plugins.gradle.IJTestEventLogger");
//            System.out.println(aClass);
//        } catch (ClassNotFoundException e) {
//            System.out.println("AJOSJIO");
//            throw new RuntimeException(e);
//        }
//        project.getGradle().getTaskGraph().whenReady(graph -> {
//            System.out.println("hcb46545v");
//            try {
//                ClassLoader loader = Class.forName("org.gradle.launcher.daemon.bootstrap.DaemonMain").getClassLoader();
//                Class<?> clazz = loader.loadClass("org.jetbrains.plugins.gradle.external.org.jetbrains.plugins.gradle.IJTestEventLogger");
//                Class<?> clazzs = Class.forName("org.jetbrains.plugins.gradle.external.org.jetbrains.plugins.gradle.IJTestEventLogger");
//                System.out.println(clazz);
//                System.out.println(clazzs);
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//
//            for (Task task : graph.getAllTasks()) {
//
//                if (task instanceof GameTestRunTask) {
//                    System.out.println("hcbv");
//                    try {
//                        ClassLoader loader = Class.forName("org.gradle.launcher.daemon.bootstrap.DaemonMain").getClassLoader();
//                        Class<?> clazz = loader.loadClass("org.jetbrains.plugins.gradle.external.org.jetbrains.plugins.gradle.IJTestEventLogger");
//                        Method logTestReportLocation = clazz.getDeclaredMethod("logTestReportLocation", String.class);
//                        Method configureTestEventLogging = clazz.getDeclaredMethod("configureTestEventLogging", Task.class);
//                        logTestReportLocation.setAccessible(true);
//                        configureTestEventLogging.setAccessible(true);
//
//                        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
//                        addURL.setAccessible(true);
//                        task.doFirst(tsk -> {
//                            try {
////                                ((GameTestRunTask) task).getRunTask().get().setClasspath(((GameTestRunTask) task).getRunTask().get().getClasspath())
//                                var urls = ((GameTestRunTask) task).getRunTask().get().getClasspath().getFiles().stream().filter(f -> f.getName().equals("idea_rt.jar")).map(f -> {
//                                    try {
//                                        return f.toURI().toURL();
//                                    } catch (MalformedURLException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                });
//
//                                var classLoader = loader;
//                                if (classLoader instanceof URLClassLoader urlClassLoader) {
//                                    urls.forEach(url -> {
//                                        try {
//                                            addURL.invoke(urlClassLoader, url);
//                                        } catch (IllegalAccessException | InvocationTargetException e) {
//                                            throw new RuntimeException(e);
//                                        }
//                                    });
//                                }
//                                else {
//                                    System.err.println("unable to enhance gradle daemon classloader with idea_rt.jar");
//                                }
//                            }
//                            catch (Throwable all) {
//                                System.err.println("unable to enhance gradle daemon classloader with idea_rt.jar");
//                                all.printStackTrace();
//                            }
//                        });
//
//                        logTestReportLocation.invoke(null, ((GameTestRunTask) task).getReports().getHtml().getEntryPoint().getPath());
//                        configureTestEventLogging.invoke(null, task);
//
//                        ((GameTestRunTask) task).getTestLogging().setShowStandardStreams(false);
//                    } catch (Throwable all) {
//                        all.printStackTrace();
//                    }
//                }
//            }
//        });
    }


    private void configureTestDataElementsVariants(Project project) {
        final TestingExtension testing = project.getExtensions().getByType(TestingExtension.class);
        final ExtensiblePolymorphicDomainObjectContainer<TestSuite> testSuites = testing.getSuites();

        testSuites.withType(GameTestSuite.class).configureEach(suite -> suite.getTargets().configureEach(target -> addTestResultsVariant(project, suite, target)));
    }

    private void addTestResultsVariant(Project project, GameTestSuite suite, GameTestSuiteTarget target) {
        final Configuration variant = project.getConfigurations().create("testResultsElementsFor" + Character.toUpperCase(target.getName().charAt(0)) + target.getName().substring(1));
        variant.setDescription("Directory containing binary results of running tests for the " + suite.getName() + " Test Suite's " + target.getName() + " target.");
        variant.setVisible(false);
        variant.setCanBeResolved(false);
        variant.setCanBeConsumed(true);
        variant.extendsFrom(project.getConfigurations().getByName(suite.getSources().getImplementationConfigurationName()),
                project.getConfigurations().getByName(suite.getSources().getRuntimeOnlyConfigurationName()));


        final ObjectFactory objects = project.getObjects();
        variant.attributes(attributes -> {
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.VERIFICATION));
            attributes.attribute(TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE, objects.named(TestSuiteName.class, suite.getName()));
            attributes.attribute(TestSuiteTargetName.TEST_SUITE_TARGET_NAME_ATTRIBUTE, objects.named(TestSuiteTargetName.class, target.getName()));
            attributes.attributeProvider(TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE, suite.getTestType().map(createNamedTestTypeAndVerifyUniqueness(project, suite)));
            attributes.attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.class, VerificationType.TEST_RESULTS));
        });

        variant.getOutgoing().artifact(
                target.getTestTask().flatMap(AbstractTestTask::getBinaryResultsDirectory),
                artifact -> artifact.setType(ArtifactTypeDefinition.DIRECTORY_TYPE)
        );
    }

    private Transformer<TestSuiteType, String> createNamedTestTypeAndVerifyUniqueness(Project project, TestSuite suite) {
        return tt -> {
            final TestSuite other = testTypesInUse.putIfAbsent(tt, suite);
            if (null != other) {
                throw new BuildException("Could not configure suite: '" + suite.getName() + "'. Another test suite: '" + other.getName() + "' uses the type: '" + tt + "' and has already been configured in project: '" + project.getName() + "'.");
            }
            return project.getObjects().named(TestSuiteType.class, tt);
        };
    }
}
