package me.mrhua269.chlorophyll.mixins;

import me.mrhua269.chlorophyll.utils.TickThread;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockableEventLoop.class)
public abstract class BlockableEventLoopMixin {
    @Shadow protected abstract Thread getRunningThread();

    /**
     * @author MrHua269
     * @reason Worldized ticking
     */
    @Overwrite
    public boolean isSameThread() {
        return Thread.currentThread() == this.getRunningThread() || TickThread.isTickThread();
    }
}
