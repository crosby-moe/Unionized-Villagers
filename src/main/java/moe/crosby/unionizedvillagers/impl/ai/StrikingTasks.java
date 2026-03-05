package moe.crosby.unionizedvillagers.impl.ai;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class StrikingTasks {
    private static final int STRIKE_LENGTH_TICKS = 20 * 60 * 20; // one ingame day

    public static Task<LivingEntity> createStart() {
        return TaskTriggerer.task(
            context -> context.group(context.queryMemoryValue(UnionizedVillagersImpl.STRIKE_START_TIME)).apply(context, strikeStartTime -> (world, entity, time) -> {
                long l = context.getValue(strikeStartTime);
                if (l + STRIKE_LENGTH_TICKS > time) {
                    entity.getBrain().doExclusively(UnionizedVillagersImpl.STRIKE);
                }
                return true;
            })
        );
    }

    public static Task<LivingEntity> createStop() {
        return TaskTriggerer.task(
            context -> context.group(context.queryMemoryValue(UnionizedVillagersImpl.STRIKE_START_TIME)).apply(context, strikeStartTime -> (world, entity, time) -> {
                long l = context.getValue(strikeStartTime);
                if (l + STRIKE_LENGTH_TICKS <= time) {
                    strikeStartTime.forget();
                    entity.getBrain().refreshActivities(world.getTimeOfDay(), world.getTime());
                }
                return true;
            })
        );
    }
}
