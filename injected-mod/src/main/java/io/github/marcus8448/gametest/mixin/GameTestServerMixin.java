package io.github.marcus8448.gametest.mixin;

import io.github.marcus8448.gametest.GradleGameTestListener;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Collection;

@Mixin(GameTestServer.class)
public abstract class GameTestServerMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void openGradleSocketConnection(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Collection collection, BlockPos blockPos, CallbackInfo ci) {
        try {
            GradleGameTestListener.channel = SocketChannel.open(new InetSocketAddress(InetAddress.getLoopbackAddress(), 33805));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "onServerExit", at = @At("RETURN"))
    private void closeGradleSocketConnection(CallbackInfo ci) {
        if (GradleGameTestListener.channel != null) {
            try {
                GradleGameTestListener.channel.close();
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }
}
