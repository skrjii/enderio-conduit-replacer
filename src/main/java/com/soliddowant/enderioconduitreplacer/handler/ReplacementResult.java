package com.soliddowant.enderioconduitreplacer.handler;

public class ReplacementResult {

    public enum Status {
        SUCCESS,
        INSUFFICIENT_AVAILABLE,
        NEEDS_CONFIRMATION_UPGRADES,
        SOURCE_NOT_FOUND,
        CATEGORY_MISMATCH,
        INVALID_ITEMS,
        NO_CONDUITS_REPLACED
    }

    private final Status status;
    private final int replacedCount;
    private final int requiredCount;
    private final int availableCount;

    private ReplacementResult(Status status, int replacedCount, int requiredCount, int availableCount) {
        this.status = status;
        this.replacedCount = replacedCount;
        this.requiredCount = requiredCount;
        this.availableCount = availableCount;
    }

    public static ReplacementResult success(int replacedCount) {
        return new ReplacementResult(Status.SUCCESS, replacedCount, 0, 0);
    }

    public static ReplacementResult insufficientAvailable(int required, int available) {
        return new ReplacementResult(Status.INSUFFICIENT_AVAILABLE, 0, required, available);
    }

    public static ReplacementResult needsConfirmationUpgrades() {
        return new ReplacementResult(Status.NEEDS_CONFIRMATION_UPGRADES, 0, 0, 0);
    }

    public static ReplacementResult sourceNotFound() {
        return new ReplacementResult(Status.SOURCE_NOT_FOUND, 0, 0, 0);
    }

    public static ReplacementResult categoryMismatch() {
        return new ReplacementResult(Status.CATEGORY_MISMATCH, 0, 0, 0);
    }

    public static ReplacementResult invalidItems() {
        return new ReplacementResult(Status.INVALID_ITEMS, 0, 0, 0);
    }

    public static ReplacementResult noConduitsReplaced() {
        return new ReplacementResult(Status.NO_CONDUITS_REPLACED, 0, 0, 0);
    }

    public Status getStatus() {
        return status;
    }

    public int getReplacedCount() {
        return replacedCount;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean needsConfirmation() {
        return status == Status.NEEDS_CONFIRMATION_UPGRADES;
    }
}
