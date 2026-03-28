package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import moe.crosby.unionizedvillagers.impl.ai.StrikingTasks;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    @WrapMethod(method = "createCoreTasks")
    private static ImmutableList<@NotNull Pair<Integer, ? extends Task<? super VillagerEntity>>> addTask(RegistryEntry<VillagerProfession> profession, float speed, Operation<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> operation) {
        ImmutableList<@NotNull Pair<Integer, ? extends Task<? super VillagerEntity>>> original = operation.call(profession, speed);

        return ImmutableList.<Pair<Integer, ? extends Task<? super VillagerEntity>>>builder().addAll(original)
            .add(Pair.of(0, StrikingTasks.createStart()))
            .build();
    }
}
