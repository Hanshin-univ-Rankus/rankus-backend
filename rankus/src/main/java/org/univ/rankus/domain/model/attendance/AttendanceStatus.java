package org.univ.rankus.domain.model.attendance;

public enum AttendanceStatus {
    PRESENT("출석"),
    ABSENT("결석"),
    LATE("지각");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPresent() {
        return this == PRESENT;
    }

    public boolean isAbsent() {
        return this == ABSENT;
    }

    public boolean isLate() {
        return this == LATE;
    }
}