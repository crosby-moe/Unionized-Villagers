package moe.crosby.unionizedvillagers.impl.fast;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import moe.crosby.unionizedvillagers.impl.mixin.WorldInvoker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLookup;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class EntitySensing {
    public static final TypeFilter<Entity, VillagerEntity> VILLAGER_FILTER = TypeFilter.instanceOf(VillagerEntity.class);
    public static final TypeFilter<Entity, ServerPlayerEntity> PLAYER_FILTER = TypeFilter.instanceOf(ServerPlayerEntity.class);
    public static final TypeFilter<Entity, HostileEntity> HOSTILE_FILTER = TypeFilter.instanceOf(HostileEntity.class);

    public static boolean isVisible(PlayerEntity player) {
        return !player.isSpectator() && !player.isInvisible() && !player.isCreative() && player.isAlive();
    }

    public static boolean isVisible(Entity entity) {
        return !entity.isInvisible() && entity.isAlive();
    }

    public static <E extends Entity> @Nullable E getFirst(World world, TypeFilter<Entity, E> type, BlockPos origin, int radius, Predicate<E> filter) {
        Box box = boxbox(origin, radius);
        MutableObject<E> reference = new MutableObject<>(null);
        EntityLookup<Entity> lookup = ((WorldInvoker) world).unionized$getEntityLookup();

        lookup.forEachIntersects(type, box, entity -> {
            if (filter.test(entity)) {
                reference.setValue(entity);
                return LazyIterationConsumer.NextIteration.ABORT;
            }

            return LazyIterationConsumer.NextIteration.CONTINUE;
        });

        return reference.get();
    }

    public static <E extends Entity> List<E> getEntities(World world, TypeFilter<Entity, E> type, BlockPos origin, int radius, Predicate<E> filter) {
        Box box = boxbox(origin, radius);
        EntityLookup<Entity> lookup = ((WorldInvoker) world).unionized$getEntityLookup();
        List<E> list = new ObjectArrayList<>();

        lookup.forEachIntersects(type, box, entity -> {
            if (filter.test(entity)) {
                list.add(entity);
            }

            return LazyIterationConsumer.NextIteration.CONTINUE;
        });

        return list;
    }

    public static <E extends Entity> void forEach(World world, TypeFilter<Entity, E> type, BlockPos origin, int radius, LazyIterationConsumer<E> consumer) {
        Box box = boxbox(origin, radius);
        EntityLookup<Entity> lookup = ((WorldInvoker) world).unionized$getEntityLookup();
        lookup.forEachIntersects(type, box, consumer);
    }

    private static Box boxbox(BlockPos origin, int radius) {
        return new Box(
            origin.getX() - radius,
            origin.getY() - radius,
            origin.getZ() - radius,
            origin.getX() + radius + 1,
            origin.getY() + radius + 1,
            origin.getZ() + radius + 1
        );
    }
}
