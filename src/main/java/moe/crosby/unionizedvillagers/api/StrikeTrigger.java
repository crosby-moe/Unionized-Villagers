package moe.crosby.unionizedvillagers.api;

public record StrikeTrigger(String translationKey, Severity severity) {
    public static StrikeTrigger create(String name, Severity severity) {
        return new StrikeTrigger("unionized-villagers.trigger." + name, severity);
    }
}
