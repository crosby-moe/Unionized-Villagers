package moe.crosby.unionizedvillagers.impl.ai;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import org.jetbrains.annotations.NotNull;

public class StrikingTasks {
    private static final int STRIKE_LENGTH_TICKS = 20 * 60 * 20; // one ingame day

    public static BehaviorControl<@NotNull LivingEntity> createStart() {
        return BehaviorBuilder.create(
            i -> i.group(
                i.present(UnionizedVillagersImpl.STRIKE_START_TIME)
            )
            .apply(
                i,
                (strikeStartTime) -> (level, body, timestamp) -> {
                    long l = i.get(strikeStartTime);
                    if (l + STRIKE_LENGTH_TICKS > timestamp) {
                        body.getBrain().setActiveActivityIfPossible(UnionizedVillagersImpl.STRIKE);
                    }
                    return true;
                }
            )
        );
    }

    public static BehaviorControl<@NotNull LivingEntity> createStop() {
        return BehaviorBuilder.create(
            i -> i.group(
                i.present(UnionizedVillagersImpl.STRIKE_START_TIME)
            )
            .apply(
                i,
                (strikeStartTime) -> (level, body, timestamp) -> {
                    long l = i.get(strikeStartTime);
                    if (l + STRIKE_LENGTH_TICKS <= timestamp) {
                        strikeStartTime.erase();
                        body.getBrain().updateActivityFromSchedule(level.environmentAttributes(), level.getGameTime(), body.position());
                    }
                    return true;
                }
            )
        );
    }
}
