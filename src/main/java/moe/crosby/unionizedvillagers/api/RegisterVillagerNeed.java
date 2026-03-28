package moe.crosby.unionizedvillagers.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RegisterVillagerNeed {
    Event<@NotNull RegisterVillagerNeed> EVENT = EventFactory.createArrayBacked(RegisterVillagerNeed.class, listeners -> registry -> {
        for (RegisterVillagerNeed event : listeners) {
            event.register(registry);
        }
    });

    void register(List<VillagerNeed> registry);
}
