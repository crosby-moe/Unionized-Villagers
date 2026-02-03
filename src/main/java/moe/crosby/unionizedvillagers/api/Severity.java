package moe.crosby.unionizedvillagers.api;

public enum Severity {
    MINOR(1),
    MAJOR(3);

    public int warningLevel;

    Severity(int warningLevel) {
        this.warningLevel = warningLevel;
    }
}
