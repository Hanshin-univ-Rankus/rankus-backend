package org.univ.rankus.domain.model.lab.notice;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeErrorCode;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeValidationException;
import org.univ.rankus.domain.model.user.User;

@Getter
@Entity
@Table(name = "lab_notices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabNotice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String content;

    @Column(nullable = false)
    private boolean pinned = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeType type = NoticeType.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    public LabNotice(String title, String content, User author, Lab lab) {
        this.title = validateTitle(title);
        this.content = validateContent(content);
        this.author = validateAuthor(author);
        this.lab = validateLab(lab);
        this.pinned = false;
        this.type = NoticeType.NORMAL;
    }

    public LabNotice(String title, String content, NoticeType type, boolean pinned, User author, Lab lab) {
        this.title = validateTitle(title);
        this.content = validateContent(content);
        this.type = validateType(type);
        this.pinned = pinned;
        this.author = validateAuthor(author);
        this.lab = validateLab(lab);
    }


    private String validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new NoticeValidationException(NoticeErrorCode.TITLE_REQUIRED);
        }
        String trimmed = title.trim();
        if (trimmed.length() > 100) {
            throw new NoticeValidationException(NoticeErrorCode.TITLE_TOO_LONG);
        }
        return trimmed;
    }

    private String validateContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new NoticeValidationException(NoticeErrorCode.CONTENT_REQUIRED);
        }
        String trimmed = content.trim();
        if (trimmed.length() > 2000) {
            throw new NoticeValidationException(NoticeErrorCode.CONTENT_TOO_LONG);
        }
        return trimmed;
    }

    private User validateAuthor(User author) {
        if (author == null) {
            throw new NoticeValidationException(NoticeErrorCode.AUTHOR_REQUIRED);
        }
        return author;
    }

    private Lab validateLab(Lab lab) {
        if (lab == null) {
            throw new NoticeValidationException(NoticeErrorCode.LAB_REQUIRED);
        }
        return lab;
    }

    private NoticeType validateType(NoticeType type) {
        if (type == null) {
            throw new NoticeValidationException(NoticeErrorCode.TYPE_REQUIRED);
        }
        return type;
    }

    public void updateTitle(String newTitle) {
        this.title = validateTitle(newTitle);
    }

    public void updateContent(String newContent) {
        this.content = validateContent(newContent);
    }

    public void updateType(NoticeType newType) {
        this.type = validateType(newType);
    }

    public void pin() {
        this.pinned = true;
    }

    public void unpin() {
        this.pinned = false;
    }

    public void togglePin() {
        this.pinned = !this.pinned;
    }

    public boolean isPinned() {
        return this.pinned;
    }

    public boolean isAuthor(User user) {
        return this.author != null && this.author.equals(user);
    }

    public boolean belongsToLab(Lab lab) {
        return this.lab != null && this.lab.equals(lab);
    }

    public boolean isUrgent() {
        return this.type == NoticeType.URGENT;
    }
}