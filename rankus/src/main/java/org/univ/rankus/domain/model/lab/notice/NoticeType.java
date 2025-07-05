package org.univ.rankus.domain.model.lab.notice;

public enum NoticeType {
    NORMAL("일반 공지"),
    URGENT("긴급 공지");

    private final String description;

    NoticeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUrgent() {
        return this == URGENT;
    }
}