package dev.galacticraft.gametest.impl;

import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.classpath.ModuleRegistry;
import org.gradle.api.internal.tasks.testing.*;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.time.Clock;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.worker.WorkerProcessFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GameTestTestExecutor implements TestExecuter<GameTestTestExecutionSpec> {
//    private final WorkerProcessFactory workerFactory;
//    private final ActorFactory actorFactory;
//    private final ModuleRegistry moduleRegistry;
//    private final WorkerLeaseService workerLeaseService;
//    private final int maxWorkerCount;
    private final Clock clock;
//    private final ExecActionFactory execActionFactory;
    //    private final DocumentationRegistry documentationRegistry;
//    private final DefaultTestFilter testFilter;
    private ExecutorService executorService;

    public GameTestTestExecutor(
            WorkerProcessFactory workerFactory, ActorFactory actorFactory, ModuleRegistry moduleRegistry,
            WorkerLeaseService workerLeaseService, int maxWorkerCount,
            Clock clock, DocumentationRegistry documentationRegistry, ExecActionFactory execActionFactory, DefaultTestFilter testFilter
    ) {
//        this.workerFactory = workerFactory;
//        this.actorFactory = actorFactory;
//        this.moduleRegistry = moduleRegistry;
//        this.workerLeaseService = workerLeaseService;
//        this.maxWorkerCount = maxWorkerCount;
        this.clock = clock;
//        this.documentationRegistry = documentationRegistry;
//        this.testFilter = testFilter;
//        this.execActionFactory = execActionFactory;
    }

    @Override
    public void execute(GameTestTestExecutionSpec testExecutionSpec, TestResultProcessor testResultProcessor) {
//        processor =
//                new PatternMatchTestClassProcessor(testFilter, new GameTestTestClassProcessor(
//                        workerLeaseService, workerFactory, testExecutionSpec, moduleRegistry, documentationRegistry)
//                        /*new RestartEveryNTestClassProcessor(, 0)*/
//                        /*new RunPreviousFailedFirstTestClassProcessor("", *//*)*/);
        executorService = Executors.newSingleThreadExecutor();
        GameTestDescriptor rootTest = new GameTestDescriptor(testExecutionSpec.getName(), null, true, testExecutionSpec.getName());
        testResultProcessor.started(rootTest, new TestStartEvent(this.clock.getCurrentTime()));

        Future<?> future = executorService.submit(() -> testExecutionSpec.getExec().exec());
        try (ServerSocketChannel socket = ServerSocketChannel.open()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(512); //overkill, but just in case
            socket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 33805));
            SocketChannel accept = socket.accept();
            Map<String, GameTestDescriptor> groupDescs = new HashMap<>();
            Map<String, GameTestDescriptor> testDescs = new HashMap<>();
            accept.read(byteBuffer);
            outer_loop:
            while (accept.isConnected() || byteBuffer.position() > 0) {
                while (byteBuffer.position() < 3) {
                    if (future.isDone()) {
                        break outer_loop;
                    }
                    try {
                        accept.read(byteBuffer);
                    } catch (Exception ignored) {
                    }
                }
                byteBuffer.flip();
                short b = byteBuffer.getShort();
                Command command = Command.getByOrdinal(byteBuffer.get());
                byteBuffer.compact();
                while (byteBuffer.position() < b) {
                    if (future.isDone()) {
                        break outer_loop;
                    }
                    try {
                        accept.read(byteBuffer);
                    } catch (Exception ignored) {
                    }
                }
                byteBuffer.flip();
                StringBuilder builder = new StringBuilder(b);
                for (int i = 0; i < b; i++) {
                    builder.append((char)byteBuffer.get());
                }
                String s = builder.toString();
                switch (command) {
                    case TESTS_STARTED_GROUP -> {
                        String[] split = s.split("\0");
                        GameTestDescriptor groupDesc = groupDescs.computeIfAbsent(split[0], rootTest::childGroup);
                        testResultProcessor.started(groupDesc, new TestStartEvent(clock.getCurrentTime()));
                        for (int i = 1; i < split.length; i++) {
                            GameTestDescriptor child = groupDesc.child(split[i], split[0]);
                            testDescs.put(split[i], child);
                            testResultProcessor.started(child, new TestStartEvent(clock.getCurrentTime()));
                        }
                    }
                    case TEST_GROUP_FINISHED -> {
                        GameTestDescriptor groupDesc = groupDescs.computeIfAbsent(s, rootTest::childGroup);
                        if (groupDesc.isFailure()) {
                            testResultProcessor.completed(groupDesc.getId(), new TestCompleteEvent(clock.getCurrentTime(), TestResult.ResultType.FAILURE));
                        } else {
                            testResultProcessor.completed(groupDesc.getId(), new TestCompleteEvent(clock.getCurrentTime()));
                        }
                    }
                    case TEST_SUCCESS -> testResultProcessor.completed(testDescs.get(s).getId(), new TestCompleteEvent(clock.getCurrentTime()));
                    case TEST_FAILURE -> {
                        GameTestDescriptor testId = testDescs.get(s);
                        testResultProcessor.completed(testId.getId(), new TestCompleteEvent(clock.getCurrentTime(), TestResult.ResultType.FAILURE));
                        testId.recordFailure();
                    }
                    case TEST_IGNORED_FAILURE -> testResultProcessor.completed(testDescs.get(s).getId(), new TestCompleteEvent(clock.getCurrentTime(), TestResult.ResultType.SKIPPED));
                }

                byteBuffer.compact();
                accept.read(byteBuffer);
            }
        } catch (IOException e) {
//            testResultProcessor.failure(rootTest.getId(), e);
//            future.cancel(true);
//            return;
        }

//        Throwable ex = null;
//
//        try {
//            future.get(2500, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException | TimeoutException e) {
//            ex = e;
//        } catch (ExecutionException e) {
////            ex = e.getCause() != null ? e.getCause() : e;
//        }
//        if (ex == null) {
//            testResultProcessor.completed(rootTest.getId(), new TestCompleteEvent(this.clock.getCurrentTime(), TestResult.ResultType.SUCCESS));
//        } else {
//            ex.printStackTrace();
//            testResultProcessor.failure(rootTest.getId(), ex);
//        }
    }

    @Override
    public void stopNow() {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
    }

    private enum Command {
        TESTS_STARTED_GROUP,
        TEST_GROUP_FINISHED,
        TEST_SUCCESS,
        TEST_FAILURE,
        TEST_IGNORED_FAILURE;

        public static Command getByOrdinal(byte b) {
            return switch (b) {
                case 0 -> TESTS_STARTED_GROUP;
                case 1 -> TEST_GROUP_FINISHED;
                case 2 -> TEST_SUCCESS;
                case 3 -> TEST_FAILURE;
                case 4 -> TEST_IGNORED_FAILURE;
                default -> throw new IllegalStateException("Unexpected value: " + b);
            };
        }
    }

    public static class GameTestDescriptor implements TestDescriptorInternal {
        private final GameTestDescriptor parent;
        private final boolean suite;
        private final String name;
        private boolean failure = false;
        private final String className;

        public GameTestDescriptor(String name, GameTestDescriptor parent, boolean suite, String className) {
            this.name = name;
            this.parent = parent;
            this.suite = suite;
            this.className = className;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDisplayName() {
            return this.name;
        }

        @Nullable
        @Override
        public String getClassName() {
            return this.className;
        }

        @Override
        public boolean isComposite() {
            return this.suite;
        }

        @Nullable
        @Override
        public TestDescriptorInternal getParent() {
            return this.parent;
        }

        @Override
        public Object getId() {
            return this.getName();
        }

        @Override
        public String getClassDisplayName() {
            return this.name;
        }

        public GameTestDescriptor child(String name, String className) {
            return new GameTestDescriptor(name, this, false, className);
        }

        public GameTestDescriptor childGroup(String name) {
            return new GameTestDescriptor(name, this, true, "group");
        }

        public void recordFailure() {
            this.failure = true;
            if (this.parent != null) {
                this.parent.recordFailure();
            }
        }

        public boolean isFailure() {
            return failure;
        }

        @Override
        public String toString() {
            return this.getId().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GameTestDescriptor that = (GameTestDescriptor) o;
            return Objects.equals(parent, that.parent) && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, name);
        }
    }
}
