package me.mrhua269.chlorophyll.mixins;

import me.mrhua269.chlorophyll.utils.EntityTaskScheduler;
import me.mrhua269.chlorophyll.utils.bridges.ITaskSchedulingEntity;
import me.mrhua269.chlorophyll.utils.bridges.ITaskSchedulingLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements ITaskSchedulingEntity {
    @Shadow public abstract List<Entity> getPassengers();

    @Shadow public abstract void ejectPassengers();

    @Shadow protected abstract TeleportTransition calculatePassengerTransition(TeleportTransition teleportTransition, Entity entity);

    @Shadow public abstract void removeAfterChangingDimensions();

    @Shadow public abstract EntityType<?> getType();

    @Unique
    @Final
    private final EntityTaskScheduler taskScheduler = new EntityTaskScheduler();

    /**
     * @author MrHua269
     * @reason Worldized ticking
     */
    @Overwrite
    private Entity teleportCrossDimension(ServerLevel serverLevel, ServerLevel serverLevel2, TeleportTransition teleportTransition) {
        List<Entity> list = this.getPassengers();
        List<Entity> list2 = new ArrayList<>(list.size());
        this.ejectPassengers();

        for (Entity entity : list) {
            Entity entity2 = entity.teleport(this.calculatePassengerTransition(teleportTransition, entity));
            if (entity2 != null) {
                list2.add(entity2);
            }
        }

        Entity entity = this.getType().create(serverLevel2, EntitySpawnReason.DIMENSION_TRAVEL);
        if (entity == null) {
            return null;
        } else {
            final Entity thisEntity = (Entity) (Object)this;

            entity.restoreFrom(thisEntity);
            this.removeAfterChangingDimensions();
            entity.teleportSetPosition(PositionMoveRotation.of(teleportTransition), teleportTransition.relatives());


            ((ITaskSchedulingLevel) serverLevel2).chlorophyll$getTickLoop().schedule(() -> {
                serverLevel2.addDuringTeleport(entity);
                serverLevel2.resetEmptyTime();

                for (Entity entity3 : list2) {
                    entity3.startRiding(entity, true);
                }

                teleportTransition.postTeleportTransition().onTransition(entity);
            });

            return entity;
        }
    }

    @Override
    public EntityTaskScheduler chlorophyll$getTaskScheduler() {
        return this.taskScheduler;
    }

    @Inject(method = "setRemoved", at = @At(value = "RETURN"))
    private void chlorophyll$onRemoved(Entity.RemovalReason cause, CallbackInfo ci) {
        if (cause != Entity.RemovalReason.CHANGED_DIMENSION && !(((Entity)(Object)this) instanceof Player)) {
            this.taskScheduler.destroy();
        }
    }
}
