package moe.crosby.unionizedvillagers.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.List;

public interface RegisterVillagerNeed {
    Event<RegisterVillagerNeed> EVENT = EventFactory.createArrayBacked(RegisterVillagerNeed.class, listeners -> registry -> {
        for (RegisterVillagerNeed event : listeners) {
            event.register(registry);
        }
    });

    void register(List<VillagerNeed> registry);
}
