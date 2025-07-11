package me.mrhua269.chlorophyll.mixins.ai;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Function;

@Mixin(BabyFollowAdult.class)
public class BabyFollowAdultMixin {
    /**
     * @author MrHua269
     * @reason Worldized ticking
     */
    @Contract("_, _, _, _ -> new")
    @Overwrite
    public static @NotNull OneShot<LivingEntity> create(UniformInt uniformInt, Function<LivingEntity, Float> function, MemoryModuleType<? extends LivingEntity> memoryModuleType, boolean bl) {
        return BehaviorBuilder.create((instance) -> instance.group(instance.present(memoryModuleType), instance.registered(MemoryModuleType.LOOK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET)).apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
            if (!livingEntity.isBaby()) {
                return false;
            } else {
                LivingEntity livingEntity2 = (LivingEntity)instance.get(memoryAccessor);

                if (livingEntity2.level() != livingEntity.level()) {
                    memoryAccessor.erase();
                    return true;
                }

                if (livingEntity.closerThan(livingEntity2, (double)(uniformInt.getMaxValue() + 1)) && !livingEntity.closerThan(livingEntity2, (double)uniformInt.getMinValue())) {
                    WalkTarget walkTarget = new WalkTarget(new EntityTracker(livingEntity2, bl, bl), (Float)function.apply(livingEntity), uniformInt.getMinValue() - 1);
                    memoryAccessor2.set(new EntityTracker(livingEntity2, true, bl));
                    memoryAccessor3.set(walkTarget);
                    return true;
                } else {
                    return false;
                }
            }
        }));
    }
}
