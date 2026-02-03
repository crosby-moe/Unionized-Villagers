package moe.crosby.unionizedvillagers.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

public class VillagerStrikeWarningManager {
    public static final Codec<VillagerStrikeWarningManager> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                Codecs.NONNEGATIVE_INT.fieldOf("ticks_since_last_warning").orElse(0).forGetter(manager -> manager.ticksSinceLastWarning),
                Codecs.NONNEGATIVE_INT.fieldOf("warning_level").orElse(0).forGetter(manager -> manager.warningLevel),
                Codecs.NONNEGATIVE_INT.fieldOf("cooldown_ticks").orElse(0).forGetter(manager -> manager.cooldownTicks)
            )
            .apply(instance, VillagerStrikeWarningManager::new)
    );
    private static final int MAX_WARNING_LEVEL = 4;
    private static final double WARN_RANGE = 32d;
    private static final int WARN_DECREASE_COOLDOWN = 12000;
    private static final int WARN_INCREASE_COOLDOWN = 200;
    private transient final Set<VillagerEntity> victims = new ObjectOpenHashSet<>();
    private int ticksSinceLastWarning;
    private int warningLevel;
    private int cooldownTicks;

    public VillagerStrikeWarningManager(int ticksSinceLastWarning, int warningLevel, int cooldownTicks) {
        this.ticksSinceLastWarning = ticksSinceLastWarning;
        this.warningLevel = warningLevel;
        this.cooldownTicks = cooldownTicks;
    }

    public void tick() {
        if (this.ticksSinceLastWarning >= WARN_DECREASE_COOLDOWN) {
            this.decreaseWarningLevel();
            this.ticksSinceLastWarning = 0;
        } else {
            this.ticksSinceLastWarning++;
        }

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
        }
    }

    public void reset() {
        this.ticksSinceLastWarning = 0;
        this.warningLevel = 0;
        this.cooldownTicks = 0;
    }

    private boolean isInCooldown() {
        return this.cooldownTicks > 0;
    }

    public boolean increaseWarningLevel(int count) {
        if (!this.isInCooldown()) {
            this.ticksSinceLastWarning = 0;
            this.cooldownTicks = WARN_INCREASE_COOLDOWN;
            this.setWarningLevel(this.getWarningLevel() + count);
            return true;
        }
        return false;
    }

    private void decreaseWarningLevel() {
        this.setWarningLevel(this.getWarningLevel() - 1);
    }

    public void setWarningLevel(int warningLevel) {
        this.warningLevel = MathHelper.clamp(warningLevel, 0, MAX_WARNING_LEVEL);
    }

    public int getWarningLevel() {
        return this.warningLevel;
    }

    public void copy(VillagerStrikeWarningManager other) {
        this.warningLevel = other.warningLevel;
        this.cooldownTicks = other.cooldownTicks;
        this.ticksSinceLastWarning = other.ticksSinceLastWarning;
        this.victims.clear();
        this.victims.addAll(other.victims);
    }
}
