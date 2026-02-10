package moe.crosby.unionizedvillagers.api;

public final class StrikeTriggers {
    public static final StrikeTrigger BREAKING_POSSESSION = StrikeTrigger.create("breaking_possession", Severity.MINOR);
    public static final StrikeTrigger KILLING_POSSESSION = StrikeTrigger.create("killing_possession", Severity.MINOR);
    public static final StrikeTrigger HARMING_VILLAGER = StrikeTrigger.create("harming_villager", Severity.MINOR);
    public static final StrikeTrigger BREAKING_WORKSPACE = StrikeTrigger.create("breaking_workspace", Severity.MINOR);
    public static final StrikeTrigger BREAKING_OWN_WORKSPACE = StrikeTrigger.create("breaking_own_workspace", Severity.MINOR);
    public static final StrikeTrigger BREAKING_STRUCTURE = StrikeTrigger.create("breaking_structure", Severity.MINOR);
    public static final StrikeTrigger ATTACKING_GUARDIAN = StrikeTrigger.create("attacking_guardian", Severity.MINOR);

    public static final StrikeTrigger KILLING_GUARDIAN = StrikeTrigger.create("killing_guardian", Severity.MAJOR);
    public static final StrikeTrigger KIDNAPPING = StrikeTrigger.create("kidnapping", Severity.MAJOR);
    public static final StrikeTrigger ZOMBIFIED = StrikeTrigger.create("zombified", Severity.MAJOR);
    public static final StrikeTrigger ILLEGAL_TRADING = StrikeTrigger.create("illegal_trading", Severity.MAJOR);
}
