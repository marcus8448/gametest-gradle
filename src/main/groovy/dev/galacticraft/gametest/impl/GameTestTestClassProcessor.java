//package dev.galacticraft.gametest.impl;
//
//import org.gradle.api.internal.DocumentationRegistry;
//import org.gradle.api.internal.classpath.ModuleRegistry;
//import org.gradle.api.internal.tasks.testing.TestClassProcessor;
//import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
//import org.gradle.api.internal.tasks.testing.TestResultProcessor;
//import org.gradle.internal.work.WorkerLeaseService;
//import org.gradle.process.internal.worker.WorkerProcessFactory;
//
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class GameTestTestClassProcessor implements TestClassProcessor {
//    private final WorkerLeaseService workerLeaseService;
//    private final WorkerProcessFactory workerFactory;
//    private final GameTestTestExecutionSpec spec;
//    private final ModuleRegistry moduleRegistry;
//    private final DocumentationRegistry documentationRegistry;
//    private TestResultProcessor resultProcessor;
//    private final Lock lock = new ReentrantLock();
//
//    public GameTestTestClassProcessor(WorkerLeaseService workerLeaseService, WorkerProcessFactory workerFactory, GameTestTestExecutionSpec testExecutionSpec, ModuleRegistry moduleRegistry, DocumentationRegistry documentationRegistry) {
//        this.workerLeaseService = workerLeaseService;
//        this.workerFactory = workerFactory;
//        this.spec = testExecutionSpec;
//        this.moduleRegistry = moduleRegistry;
//        this.documentationRegistry = documentationRegistry;
//    }
//
//    @Override
//    public void startProcessing(TestResultProcessor resultProcessor) {
//        this.resultProcessor = resultProcessor;
//    }
//
//    @Override
//    public void processTestClass(TestClassRunInfo testClass) {
//        testClass.getTestClassName()
//    }
//
//    @Override
//    public void stop() {
//        if (h)
//    }
//
//    @Override
//    public void stopNow() {
//
//    }
//}
