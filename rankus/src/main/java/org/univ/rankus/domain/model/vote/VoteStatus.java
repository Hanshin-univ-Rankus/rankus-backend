package org.univ.rankus.domain.model.vote;

public enum VoteStatus {
    ACTIVE("진행중"),
    CLOSED("종료"),
    CANCELED("취소됨");

    private final String description;

    VoteStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isCanceled() {
        return this == CANCELED;
    }

    public boolean canTransitionTo(VoteStatus newStatus) {
        return switch (this) {
            case ACTIVE -> newStatus == CLOSED || newStatus == CANCELED;
            case CLOSED, CANCELED -> false;
        };
    }
}