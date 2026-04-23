package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import moe.crosby.unionizedvillagers.impl.ai.StrikingTasks;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VillagerGoalPackages.class)
public class VillagerGoalPackagesMixin {
    @WrapMethod(method = "getCorePackage")
    private static ImmutableList<@NotNull Pair<Integer, ? extends BehaviorControl<? super Villager>>> addTask(Holder<@NotNull VillagerProfession> profession, float speed, Operation<ImmutableList<@NotNull Pair<Integer, ? extends BehaviorControl<? super Villager>>>> operation) {
        ImmutableList<@NotNull Pair<Integer, ? extends BehaviorControl<? super Villager>>> original = operation.call(profession, speed);

        return ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super Villager>>>builder().addAll(original)
            .add(Pair.of(0, StrikingTasks.createStart()))
            .build();
    }
}
