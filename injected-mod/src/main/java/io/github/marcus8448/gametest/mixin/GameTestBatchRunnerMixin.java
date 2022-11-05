package io.github.marcus8448.gametest.mixin;

import com.mojang.datafixers.util.Pair;
import io.github.marcus8448.gametest.GradleGameTestListener;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchRunner;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.MultipleTestTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Map;

@Mixin(GameTestBatchRunner.class)
public abstract class GameTestBatchRunnerMixin {
    @Inject(method = "runBatch", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initTracker(int i, CallbackInfo ci, Pair pair, GameTestBatch gameTestBatch, Collection<GameTestInfo> collection, Map map, String string, MultipleTestTracker multipleTestTracker) {
        multipleTestTracker.addListener(new GradleGameTestListener(string, collection));
    }
}
