package me.mrhua269.chlorophyll.mixins;

import me.mrhua269.chlorophyll.utils.TickThread;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {
    @Shadow @Final private ServerLevel level;

    @Inject(method = "tryRespawn", at = @At(value = "HEAD"), cancellable = true)
    public void onTryRespawn(CallbackInfo ci) {
        final TickThread curr = TickThread.currentThread();

        if (curr == null || curr.currentTickLoop == null) {
            // not in a tick loop, cancel directly
            ci.cancel();
            return;
        }

        if (curr.currentTickLoop.getOwnedLevel() != this.level) {
            // not in the same world, cancel directly
            ci.cancel();
        }
    }
}
