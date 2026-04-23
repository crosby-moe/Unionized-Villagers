package moe.crosby.unionizedvillagers.impl.mixin;

import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomizableContainerBlockEntity.class)
public interface RandomizableContainerBlockEntityAccessor {
    @Accessor("lootTable")
    ResourceKey<@NotNull LootTable> unionized$getLootTable();
}
