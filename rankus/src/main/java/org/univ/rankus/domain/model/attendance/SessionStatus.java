package org.univ.rankus.domain.model.attendance;

public enum SessionStatus {
    ACTIVE("활성"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String description;

    SessionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }
}