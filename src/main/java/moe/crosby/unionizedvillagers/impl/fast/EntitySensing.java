package moe.crosby.unionizedvillagers.impl.fast;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import moe.crosby.unionizedvillagers.impl.mixin.LevelInvoker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class EntitySensing {
    public static final EntityTypeTest<@NotNull Entity, @NotNull Villager> VILLAGER_FILTER = EntityTypeTest.forClass(Villager.class);
    public static final EntityTypeTest<@NotNull Entity, @NotNull ServerPlayer> PLAYER_FILTER = EntityTypeTest.forClass(ServerPlayer.class);
    public static final EntityTypeTest<@NotNull Entity, @NotNull Monster> HOSTILE_FILTER = EntityTypeTest.forClass(Monster.class);

    public static boolean isVisible(Player player) {
        return !player.isSpectator() && !player.isInvisible() && !player.isCreative() && player.isAlive();
    }

    public static boolean isVisible(Entity entity) {
        return !entity.isInvisible() && entity.isAlive();
    }

    public static <E extends Entity> @Nullable E getFirst(Level world, EntityTypeTest<@NotNull Entity, @NotNull E> type, BlockPos origin, int radius, Predicate<E> filter) {
        AABB box = boxbox(origin, radius);
        MutableObject<E> reference = new MutableObject<>(null);
        LevelEntityGetter<@NotNull Entity> lookup = ((LevelInvoker) world).unionized$getEntities();

        lookup.get(type, box, entity -> {
            if (filter.test(entity)) {
                reference.setValue(entity);
                return AbortableIterationConsumer.Continuation.ABORT;
            }

            return AbortableIterationConsumer.Continuation.CONTINUE;
        });

        return reference.get();
    }

    public static <E extends Entity> List<E> getEntities(Level world, EntityTypeTest<@NotNull Entity, @NotNull E> type, BlockPos origin, int radius, Predicate<E> filter) {
        AABB box = boxbox(origin, radius);
        LevelEntityGetter<@NotNull Entity> lookup = ((LevelInvoker) world).unionized$getEntities();
        List<E> list = new ObjectArrayList<>();

        lookup.get(type, box, entity -> {
            if (filter.test(entity)) {
                list.add(entity);
            }

            return AbortableIterationConsumer.Continuation.CONTINUE;
        });

        return list;
    }

    public static <E extends Entity> void forEach(Level world, EntityTypeTest<@NotNull Entity, @NotNull E> type, BlockPos origin, int radius, AbortableIterationConsumer<@NotNull E> consumer) {
        AABB box = boxbox(origin, radius);
        LevelEntityGetter<@NotNull Entity> lookup = ((LevelInvoker) world).unionized$getEntities();
        lookup.get(type, box, consumer);
    }

    private static AABB boxbox(BlockPos origin, int radius) {
        return new AABB(
            origin.getX() - radius,
            origin.getY() - radius,
            origin.getZ() - radius,
            origin.getX() + radius + 1,
            origin.getY() + radius + 1,
            origin.getZ() + radius + 1
        );
    }
}
