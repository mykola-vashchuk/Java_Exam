package ua.ukma.edu.domain;

public enum Status {
    CREATED,
    CONFIRMED,
    COMPLETED,
    CANCELLED;

    public boolean isActive() {
        return this == CREATED || this == CONFIRMED;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean canTransitionTo(Status next) {
        if (next == null) {
            return false;
        }
        return switch (this) {
            case CREATED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == COMPLETED || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
