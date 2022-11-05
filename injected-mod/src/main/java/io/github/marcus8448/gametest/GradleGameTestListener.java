package io.github.marcus8448.gametest;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GradleGameTestListener implements GameTestListener {
    public static final ThreadLocal<ByteBuffer> BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocate(1024));
    public static SocketChannel channel = null; //TODO: better I/O solution (or just better buffer handling)
    private final String groupName;
    private final List<String> tests;

    public GradleGameTestListener(String groupName, Collection<GameTestInfo> collection) {
        this.groupName = groupName;
        this.tests = new ArrayList<>(collection.size());
        for (GameTestInfo gameTestInfo : collection) {
            tests.add(gameTestInfo.getTestName());
        }
        try {
            channel.finishConnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteBuffer byteBuffer = BUFFER.get();
        byteBuffer.clear();
        byte[] bytes = groupName.getBytes();
        short len = (short) (bytes.length);
        for (GameTestInfo gameTestInfo : collection) {
            len += 1 + gameTestInfo.getTestName().length();
        }
        assert len > 0;
        byteBuffer.putShort(len);
        byteBuffer.put((byte) Command.TESTS_STARTED_GROUP.ordinal());
        byteBuffer.put(bytes);
        for (GameTestInfo gameTestInfo : collection) {
            byte[] bytes1 = gameTestInfo.getTestName().getBytes();
            byteBuffer.put((byte)0);
            byteBuffer.put(bytes1);
        }
        try {
            byteBuffer.flip();
            channel.write(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void testStructureLoaded(GameTestInfo gameTestInfo) {}

    @Override
    public void testPassed(GameTestInfo gameTestInfo) {
        try {
            channel.finishConnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteBuffer byteBuffer = BUFFER.get();
        byteBuffer.clear();
        byte[] bytes = gameTestInfo.getTestName().getBytes();
        byteBuffer.putShort((short)(bytes.length));
        byteBuffer.put((byte) Command.TEST_SUCCESS.ordinal());
        byteBuffer.put(bytes);
        try {
            byteBuffer.flip();
            channel.write(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        testFinish(gameTestInfo.getTestName());
    }

    @Override
    public void testFailed(GameTestInfo gameTestInfo) {
        try {
            channel.finishConnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteBuffer byteBuffer = BUFFER.get();
        byteBuffer.clear();
        byte[] bytes = gameTestInfo.getTestName().getBytes();
        byteBuffer.putShort((short)(bytes.length));
        if (gameTestInfo.isRequired()) {
            byteBuffer.put((byte) Command.TEST_FAILURE.ordinal());
        } else {
            byteBuffer.put((byte) Command.TEST_IGNORED_FAILURE.ordinal());
        }
        byteBuffer.put(bytes);
        try {
            byteBuffer.flip();
            channel.write(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        testFinish(gameTestInfo.getTestName());
    }

    private void testFinish(String test) {
        ByteBuffer byteBuffer = BUFFER.get();
        tests.remove(test);
        if (tests.isEmpty()) {
            byteBuffer.clear();
            byte[] bytes = this.groupName.getBytes();
            byteBuffer.putShort((short)(bytes.length));
            byteBuffer.put((byte) Command.TEST_GROUP_FINISHED.ordinal());
            byteBuffer.put(bytes);
            try {
                byteBuffer.flip();
                channel.write(byteBuffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public enum Command {
        TESTS_STARTED_GROUP,
        TEST_GROUP_FINISHED,
        TEST_SUCCESS,
        TEST_FAILURE,
        TEST_IGNORED_FAILURE;
    }
}
